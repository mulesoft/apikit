/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.DefaultMuleMessage;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;
import org.mule.message.ds.StringDataSource;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.InvalidFormParameterException;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.transform.TransformerCache;
import org.mule.module.apikit.validation.RestSchemaValidator;
import org.mule.module.apikit.validation.RestSchemaValidatorFactory;
import org.mule.module.apikit.validation.cache.SchemaCacheUtils;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;
import org.mule.transport.http.transformers.FormTransformer;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Collection;
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

    private MuleEvent requestEvent;
    private Configuration config;
    private Action action;
    private HttpProtocolAdapter adapter;

    public HttpRestRequest(MuleEvent event, Configuration config)
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
        String basePath = adapter.getBaseURI().getPath();
        int start = basePath.endsWith("/") ? basePath.length() - 1 : basePath.length();
        int end = path.endsWith("/") ? path.length() - 1 : path.length();
        return path.substring(start, end);
    }

    public String getMethod()
    {
        return adapter.getMethod().toLowerCase();
    }

    public MuleEvent process(Flow flow, Action action) throws MuleException
    {
        this.action = action;
        if (!config.isDisableValidations())
        {
            processQueryParameters();
            processHeaders();
        }
        negotiateInputRepresentation();
        String responseRepresentation = negotiateOutputRepresentation();
        MuleEvent responseEvent = flow.process(requestEvent);

        if (responseRepresentation != null)
        {
            transformToExpectedContentType(responseEvent, responseRepresentation);
        }
        else
        {
            //sent empty response body when no response mime-type is defined
            responseEvent.getMessage().setPayload(NullPayload.getInstance());
        }

        //set success status
        if (responseEvent.getMessage().getOutboundProperty("http.status") == null)
        {
            int status = getSuccessStatus();
            if (status == -1)
            {
                throw new ApikitRuntimeException("No success status defined for action: " + action);
            }
            responseEvent.getMessage().setOutboundProperty("http.status", getSuccessStatus());
        }

        return responseEvent;
    }

    private void processQueryParameters() throws InvalidQueryParameterException
    {
        for (String expectedKey : action.getQueryParameters().keySet())
        {
            QueryParameter expected = action.getQueryParameters().get(expectedKey);
            String actual = (String) ((Map) requestEvent.getMessage().getInboundProperty("http.query.params")).get(expectedKey);
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
                if (!expected.validate(actual))
                {
                    throw new InvalidQueryParameterException("Invalid query parameter value " + actual + " for " + expectedKey);
                }
            }
        }
    }

    private void setQueryParameter(String key, String value)
    {
        requestEvent.getMessage().setProperty(key, value, PropertyScope.INBOUND);
        ((Map) requestEvent.getMessage().getInboundProperty("http.query.params")).put(key, value);
    }

    private void processHeaders() throws InvalidHeaderException
    {
        for (String expectedKey : action.getHeaders().keySet())
        {
            Header expected = action.getHeaders().get(expectedKey);
            Map<String, String> incomingHeaders = requestEvent.getMessage().getInboundProperty("http.headers");

            if (expectedKey.contains("{?}"))
            {
                String regex = expectedKey.replace("{?}", ".*");
                for (String incoming : incomingHeaders.keySet())
                {
                    String incomingValue = incomingHeaders.get(incoming);
                    if (incoming.matches(regex) && !expected.validate(incomingValue))
                    {
                        throw new InvalidHeaderException("Invalid header value " + incomingValue + " for " + expectedKey);
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
                        throw new InvalidHeaderException("Invalid header value " + actual + " for " + expectedKey);
                    }
                }
            }
        }
    }

    private void setHeader(String key, String value)
    {
        requestEvent.getMessage().setProperty(key, value, PropertyScope.INBOUND);
        ((Map) requestEvent.getMessage().getInboundProperty("http.headers")).put(key, value);
    }

    private void transformToExpectedContentType(MuleEvent muleEvent, String responseRepresentation) throws MuleException
    {
        MuleMessage message = muleEvent.getMessage();
        String msgMimeType = message.getDataType() != null ? message.getDataType().getMimeType() : null;
        String msgContentType = message.getOutboundProperty("Content-Type");

        // user is in charge of setting content-type when using */*
        if ("*/*".equals(responseRepresentation))
        {
            if (msgContentType == null)
            {
                throw new ApikitRuntimeException("Content-Type must be set in the flow when declaring */* response type");
            }
            responseRepresentation = msgContentType;
        }

        message.setOutboundProperty("Content-Type", responseRepresentation);

        if (message.getPayload() instanceof NullPayload)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Response transformation not required. Message payload type is NullPayload");
            }
            return;
        }

        if (msgMimeType != null && msgMimeType.contains(responseRepresentation) ||
            msgContentType != null && msgContentType.contains(responseRepresentation))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Response transformation not required. Message payload type is " + msgMimeType);
            }
            return;
        }
        DataType sourceDataType = DataTypeFactory.create(message.getPayload().getClass(), msgMimeType);
        DataType resultDataType = DataTypeFactory.create(String.class, responseRepresentation);

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Resolving transformer between [source=%s] and [result=%s]", sourceDataType, resultDataType));
        }

        Transformer transformer;
        try
        {
            transformer = TransformerCache.getTransformerCache(muleEvent.getMuleContext()).get(new DataTypePair(sourceDataType, resultDataType));
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Transformer resolved to [transformer=%s]", transformer));
            }
            Object payload = transformer.transform(message.getPayload());
            message.setPayload(payload);
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(e);
        }

    }

    private void negotiateInputRepresentation() throws MuleRestException
    {
        if (action == null || action.getBody().isEmpty())
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
            throw new UnsupportedMediaTypeException();
        }
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

    private void validateUrlencodedForm(Map<String, List<FormParameter>> formParameters) throws BadRequestException
    {
        Map paramMap;
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
                    throw new InvalidFormParameterException("Invalid form parameter value " + actual + " for " + expectedKey);
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
        validator.validate(SchemaCacheUtils.getSchemaCacheKey(action, mimeTypeName), requestEvent, config.getApi());
    }

    private String negotiateOutputRepresentation() throws MuleRestException
    {
        List<MimeType> mimeTypes = getResponseMimeTypes();
        if (action == null || action.getResponses() == null || mimeTypes.isEmpty())
        {
            //no response media-types defined, return no body
            return null;
        }
        MediaType bestMatch = RestContentTypeParser.bestMatch(mimeTypes, adapter.getAcceptableResponseMediaTypes());
        if (bestMatch == null)
        {
            throw new NotAcceptableException();
        }
        logger.debug("=== negotiated response content-type: " + bestMatch.toString());
        for (MimeType representation : mimeTypes)
        {
            if (representation.getType().equals(bestMatch.withoutParameters().toString()))
            {
                return representation.getType();
            }
        }
        throw new NotAcceptableException();
    }

    private List<MimeType> getResponseMimeTypes()
    {
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        int status = getSuccessStatus();
        if (status != -1)
        {
            Response response = action.getResponses().get(String.valueOf(status));
            if (response != null)
            {
                Collection<MimeType> types = response.getBody().values();
                logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, types));
                mimeTypes.addAll(types);
            }
        }
        return mimeTypes;
    }

    private int getSuccessStatus()
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
