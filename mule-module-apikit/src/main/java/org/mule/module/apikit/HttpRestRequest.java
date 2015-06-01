/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.transform.ApikitResponseTransformer.APIKIT_ROUTER_REQUEST;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.BEST_MATCH_REPRESENTATION;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.CONTRACT_MIME_TYPES;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.message.ds.StringDataSource;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.validation.RestSchemaValidator;
import org.mule.module.apikit.validation.RestSchemaValidatorFactory;
import org.mule.module.apikit.validation.SchemaType;
import org.mule.module.apikit.validation.cache.SchemaCacheUtils;
import org.mule.module.http.internal.ParameterMap;
import org.mule.transport.http.transformers.FormTransformer;
import org.mule.util.CaseInsensitiveHashMap;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestRequest
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected MuleEvent requestEvent;
    protected AbstractConfiguration config;
    protected Action action;
    protected HttpProtocolAdapter adapter;

    public HttpRestRequest(MuleEvent event, AbstractConfiguration config)
    {
        requestEvent = event;
        this.config = config;
        adapter = new HttpProtocolAdapter(event);
    }

    public HttpProtocolAdapter getAdapter()
    {
        return adapter;
    }

    public String getResourcePath()
    {
        String path = adapter.getResourceURI().getPath();
        String basePath = adapter.getBasePath();
        int start = basePath.endsWith("/") ? basePath.length() - 1 : basePath.length();
        int end = path.endsWith("/") ? path.length() - 1 : path.length();
        return URICoder.decode(path.substring(start, end));
    }

    public String getMethod()
    {
        return adapter.getMethod().toLowerCase();
    }

    public String getContentType()
    {
        return adapter.getRequestMediaType();
    }

    /**
     * Validates the request against the RAML and negotiates the response representation.
     * The resulting event is only updated when default values are applied.
     *
     * @param action Raml action to be invoked
     * @return the updated Mule Event
     * @throws MuleException
     */
    public MuleEvent validate(Action action) throws MuleException
    {
        this.action = action;
        if (!config.isDisableValidations())
        {
            processQueryParameters();
            processHeaders();
        }
        negotiateInputRepresentation();
        List<MimeType> responseMimeTypes = getResponseMimeTypes();
        String responseRepresentation = negotiateOutputRepresentation(responseMimeTypes);

        if (responseMimeTypes != null)
        {
            requestEvent.getMessage().setInvocationProperty(CONTRACT_MIME_TYPES, responseMimeTypes);
        }
        if (responseRepresentation != null)
        {
            requestEvent.getMessage().setInvocationProperty(BEST_MATCH_REPRESENTATION, responseRepresentation);
        }
        requestEvent.getMessage().setInvocationProperty(APIKIT_ROUTER_REQUEST, "yes");
        return requestEvent;
    }

    private void processQueryParameters() throws InvalidQueryParameterException
    {
        for (String expectedKey : action.getQueryParameters().keySet())
        {
            QueryParameter expected = action.getQueryParameters().get(expectedKey);
            Object actual = ((Map) requestEvent.getMessage().getInboundProperty("http.query.params")).get(expectedKey);
            if (actual == null && expected.isRequired())
            {
                throw new InvalidQueryParameterException("Required query parameter " + expectedKey + " not specified");
            }
            if (actual == null && expected.getDefaultValue() != null)
            {
                setQueryParameter(expectedKey, expected.getDefaultValue());
            }
            if (actual != null)
            {
                if (actual instanceof Collection && !expected.isRepeat())
                {
                    throw new InvalidQueryParameterException("Query parameter " + expectedKey + " is not repeatable");
                }
                if (!(actual instanceof Collection))
                {
                    actual = Collections.singletonList(actual);
                }
                //noinspection unchecked
                for (String param : (Collection<String>) actual)
                {
                    if (!expected.validate(param))
                    {
                        String msg = String.format("Invalid value '%s' for query parameter %s. %s",
                                                   param, expectedKey, expected.message(param));
                        throw new InvalidQueryParameterException(msg);
                    }
                }
            }
        }
    }

    private void setQueryParameter(String key, String value)
    {
        if (requestEvent.getMessage().getInboundProperty("http.headers") != null)
        {
            //only set query param as top-level inbound property when using endpoints instead of listeners
            requestEvent.getMessage().setProperty(key, value, PropertyScope.INBOUND);
        }
        Map<String, String> queryParamMap = requestEvent.getMessage().getInboundProperty("http.query.params");
        if (queryParamMap instanceof ParameterMap)
        {
            //overwrite the query-param map with a mutable instance
            queryParamMap = new HashMap<String, String>(queryParamMap);
            requestEvent.getMessage().setProperty("http.query.params", queryParamMap, PropertyScope.INBOUND);
        }
        queryParamMap.put(key, value);
    }


    @SuppressWarnings("unchecked")
    private void processHeaders() throws InvalidHeaderException
    {
        for (String expectedKey : action.getHeaders().keySet())
        {
            Header expected = action.getHeaders().get(expectedKey);
            Map<String, String> incomingHeaders = getIncomingHeaders(requestEvent.getMessage());

            if (expectedKey.contains("{?}"))
            {
                String regex = expectedKey.replace("{?}", ".*");
                for (String incoming : incomingHeaders.keySet())
                {
                    String incomingValue = incomingHeaders.get(incoming);
                    if (incoming.matches(regex) && !expected.validate(incomingValue))
                    {
                        String msg = String.format("Invalid value '%s' for header %s. %s",
                                                   incomingValue, expectedKey, expected.message(incomingValue));
                        throw new InvalidHeaderException(msg);
                    }
                }
            }
            else
            {
                String actual = incomingHeaders.get(expectedKey);
                if (actual == null && expected.isRequired())
                {
                    throw new InvalidHeaderException("Required header " + expectedKey + " not specified");
                }
                if (actual == null && expected.getDefaultValue() != null)
                {
                    setHeader(expectedKey, expected.getDefaultValue());
                }
                if (actual != null)
                {
                    if (!expected.validate(actual))
                    {
                        String msg = String.format("Invalid value '%s' for header %s. %s",
                                                   actual, expectedKey, expected.message(actual));
                        throw new InvalidHeaderException(msg);
                    }
                }
            }
        }
    }

    private Map<String,String> getIncomingHeaders(MuleMessage message)
    {

        Map<String, String> incomingHeaders = new CaseInsensitiveHashMap();
        if (message.getInboundProperty("http.headers") != null)
        {
            incomingHeaders = new CaseInsensitiveHashMap(message.<Map>getInboundProperty("http.headers"));
        }
        else
        {
            for (String key : message.getInboundPropertyNames())
            {
                if (!key.startsWith("http.")) //TODO MULE-8131
                {
                    incomingHeaders.put(key, String.valueOf(message.getInboundProperty(key)));
                }
            }
        }
        return incomingHeaders;
    }

    private void setHeader(String key, String value)
    {
        requestEvent.getMessage().setProperty(key, value, PropertyScope.INBOUND);
        if (requestEvent.getMessage().getInboundProperty("http.headers") != null)
        {
            //TODO MULE-8131
            requestEvent.getMessage().<Map<String, String>>getInboundProperty("http.headers").put(key, value);
        }
    }

    private void negotiateInputRepresentation() throws MuleRestException
    {
        if (action == null || !action.hasBody())
        {
            logger.debug("=== no body types defined: accepting any request content-type");
            return;
        }
        String requestMimeTypeName = null;
        boolean found = false;
        if (adapter.getRequestMediaType() != null)
        {
            requestMimeTypeName = adapter.getRequestMediaType();
        }
        for (String mimeTypeName : action.getBody().keySet())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing request media type %s with expected %s\n",
                                           requestMimeTypeName, mimeTypeName));
            }
            if (mimeTypeName.equals(requestMimeTypeName))
            {
                found = true;
                if (!config.isDisableValidations())
                {
                    valideateBody(mimeTypeName);
                }
                break;
            }
        }
        if (!found)
        {
            handleUnsupportedMediaType();
        }
    }

    protected void handleUnsupportedMediaType() throws UnsupportedMediaTypeException
    {
        throw new UnsupportedMediaTypeException();
    }

    private void valideateBody(String mimeTypeName) throws MuleRestException
    {
        MimeType actionMimeType = action.getBody().get(mimeTypeName);
        if (actionMimeType.getSchema() != null &&
            (mimeTypeName.contains("xml") ||
             mimeTypeName.contains("json")))
        {
            validateSchema(mimeTypeName);
        }
        else if (actionMimeType.getFormParameters() != null &&
                 mimeTypeName.contains("multipart/form-data"))
        {
            validateMultipartForm(actionMimeType.getFormParameters());
        }
        else if (actionMimeType.getFormParameters() != null &&
                 mimeTypeName.contains("application/x-www-form-urlencoded"))
        {
            validateUrlencodedForm(actionMimeType.getFormParameters());
        }
    }

    @SuppressWarnings("unchecked")
    private void validateUrlencodedForm(Map<String, List<FormParameter>> formParameters) throws BadRequestException
    {
        Map<String, String> paramMap;
        try
        {
            paramMap = (Map) new FormTransformer().transformMessage(requestEvent.getMessage(), requestEvent.getEncoding());
        }
        catch (TransformerException e)
        {
            logger.warn("Cannot validate url-encoded form", e);
            return;
        }

        for (String expectedKey : formParameters.keySet())
        {
            if (formParameters.get(expectedKey).size() != 1)
            {
                //do not perform validation when multi-type parameters are used
                continue;
            }

            FormParameter expected = formParameters.get(expectedKey).get(0);
            Object actual = paramMap.get(expectedKey);
            if (actual == null && expected.isRequired())
            {
                throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
            }
            if (actual == null && expected.getDefaultValue() != null)
            {
                paramMap.put(expectedKey, expected.getDefaultValue());
            }
            if (actual != null && actual instanceof String)
            {
                if (!expected.validate((String) actual))
                {
                    String msg = String.format("Invalid value '%s' for form parameter %s. %s",
                                               actual, expectedKey, expected.message((String) actual));
                    throw new InvalidQueryParameterException(msg);
                }
            }
        }
        requestEvent.getMessage().setPayload(paramMap);
    }

    private void validateMultipartForm(Map<String, List<FormParameter>> formParameters) throws BadRequestException
    {
        for (String expectedKey : formParameters.keySet())
        {
            if (formParameters.get(expectedKey).size() != 1)
            {
                //do not perform validation when multi-type parameters are used
                continue;
            }
            FormParameter expected = formParameters.get(expectedKey).get(0);
            DataHandler dataHandler = requestEvent.getMessage().getInboundAttachment(expectedKey);
            if (dataHandler == null && expected.isRequired())
            {
                //perform only 'required' validation to avoid consuming the stream
                throw new InvalidFormParameterException("Required form parameter " + expectedKey + " not specified");
            }
            if (dataHandler == null && expected.getDefaultValue() != null)
            {
                DataHandler defaultDataHandler = new DataHandler(new StringDataSource(expected.getDefaultValue(), expectedKey));
                try
                {
                    ((DefaultMuleMessage) requestEvent.getMessage()).addInboundAttachment(expectedKey, defaultDataHandler);
                }
                catch (Exception e)
                {
                    logger.warn("Cannot set default part " + expectedKey, e);
                }
            }
        }
    }

    private void validateSchema(String mimeTypeName) throws MuleRestException
    {
        SchemaType schemaType = mimeTypeName.contains("json") ? SchemaType.JSONSchema : SchemaType.XMLSchema;
        RestSchemaValidator validator = RestSchemaValidatorFactory.getInstance().createValidator(schemaType, requestEvent.getMuleContext());
        validator.validate(config.getName(), SchemaCacheUtils.getSchemaCacheKey(action, mimeTypeName), requestEvent, config.getApi());
    }

    private String negotiateOutputRepresentation(List<MimeType> mimeTypes) throws MuleRestException
    {
        if (action == null || action.getResponses() == null || mimeTypes.isEmpty())
        {
            //no response media-types defined, return no body
            return null;
        }
        MediaType bestMatch = RestContentTypeParser.bestMatch(mimeTypes, adapter.getAcceptableResponseMediaTypes());
        if (bestMatch == null)
        {
            return handleNotAcceptable();
        }
        logger.debug("=== negotiated response content-type: " + bestMatch.toString());
        for (MimeType representation : mimeTypes)
        {
            if (representation.getType().equals(bestMatch.withoutParameters().toString()))
            {
                return representation.getType();
            }
        }
        return handleNotAcceptable();
    }

    protected String handleNotAcceptable() throws NotAcceptableException
    {
        throw new NotAcceptableException();
    }

    private List<MimeType> getResponseMimeTypes()
    {
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        int status = getSuccessStatus();
        if (status != -1)
        {
            Response response = action.getResponses().get(String.valueOf(status));
            if (response != null && response.hasBody())
            {
                Collection<MimeType> types = response.getBody().values();
                logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, types));
                mimeTypes.addAll(types);
            }
        }
        return mimeTypes;
    }

    protected int getSuccessStatus()
    {
        for (String status : action.getResponses().keySet())
        {
            int code = Integer.parseInt(status);
            if (code >= 200 && code < 300)
            {
                return code;
            }
        }
        //default success status
        return 200;
    }
}
