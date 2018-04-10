/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static java.util.Collections.singletonList;
import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.module.apikit.CharsetUtils.trimBom;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.ACCEPT_HEADER;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.APIKIT_ROUTER_REQUEST;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.BEST_MATCH_REPRESENTATION;
import static org.mule.module.apikit.transform.ApikitResponseTransformer.CONTRACT_MIME_TYPES;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
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
import org.mule.raml.implv2.v10.model.MimeTypeImpl;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;
import org.mule.transport.http.transformers.FormTransformer;
import org.mule.util.CaseInsensitiveHashMap;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.raml.v2.api.model.common.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestRequest
{

    private static final List<Integer> DEFAULT_SUCCESS_STATUS = Arrays.asList(200);
    protected static final Logger logger = LoggerFactory.getLogger(HttpRestRequest.class);

    protected MuleEvent requestEvent;
    protected AbstractConfiguration config;
    protected IAction action;
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
    public MuleEvent validate(IAction action) throws MuleException
    {
        this.action = action;
        if (!config.isDisableValidations())
        {
            processQueryParameters();
            processHeaders();
        }
        negotiateInputRepresentation();
        List<String> responseMimeTypes = getResponseMimeTypes();
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
        requestEvent.getMessage().setInvocationProperty(ACCEPT_HEADER, adapter.getAcceptableResponseMediaTypes());
        return requestEvent;
    }

    private void processQueryParameters() throws InvalidQueryParameterException
    {
        for (String expectedKey : action.getQueryParameters().keySet())
        {
            IParameter expected = action.getQueryParameters().get(expectedKey);
            Collection<?> actual = getActualQueryParam(expectedKey);

            if (actual.isEmpty())
            {
                if (expected.isRequired())
                {
                    throw new InvalidQueryParameterException("Required query parameter " + expectedKey + " not specified");
                }

                if (expected.getDefaultValue() != null)
                {
                    setQueryParameter(expectedKey, expected.getDefaultValue());

                }
            }
            else
            {

                if (actual.size() > 1 && !(expected.isRepeat() || expected.isArray()))
                {
                    throw new InvalidQueryParameterException("Query parameter " + expectedKey + " is not repeatable");
                }

                if (expected.isArray())
                {
                    // raml 1.0 array validation
                    validateQueryParamArray(expectedKey, expected, actual);
                }
                else
                {
                    // single query param or repeat
                    //noinspection unchecked
                    for (String param : (Collection<String>) actual)
                    {
                        validateQueryParam(expectedKey, expected, param);
                    }
                }
            }
        }
    }

    private Collection<?> getActualQueryParam(String expectedKey)
    {
        Object queryParamsMap = requestEvent.getMessage().getInboundProperty("http.query.params");
        Collection<?> actual;
        actual = Collections.emptyList();
        if (queryParamsMap instanceof ParameterMap)
        {
            actual = ((ParameterMap) queryParamsMap).getAll(expectedKey);
        }
        else
        {
            Object param = ((Map) queryParamsMap).get(expectedKey);
            if (param instanceof Collection)
            {
                actual = (Collection<?>) param;
            }
            else if (param != null)
            {
                actual = ImmutableList.of(param);
            }
        }
        return actual;
    }

    //only for raml 1.0
    private void validateQueryParamArray(String paramKey, IParameter expected, Collection<?> paramValues) throws InvalidQueryParameterException
    {
        StringBuilder builder = new StringBuilder();
        for (Object paramValue : paramValues)
        {
            final String value = String.valueOf(paramValue);
            builder.append("- ");

            if (value.startsWith("*") || expected.isStringArray()) builder.append("\"").append(value).append("\"");
            else builder.append(value);

            builder.append("\n");
        }
        validateQueryParam(paramKey, expected, builder.toString());
    }

    private void validateQueryParam(String paramKey, IParameter expected, String paramValue) throws InvalidQueryParameterException
    {
        if (!expected.validate(paramValue))
        {
            String msg = String.format("Invalid value '%s' for query parameter %s. %s",
                                       paramValue, paramKey, expected.message(paramValue));
            throw new InvalidQueryParameterException(msg);
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
        final Map<String, Object> incomingHeaders = getIncomingHeaders(requestEvent.getMessage());
        for (String expectedKey : action.getHeaders().keySet())
        {
            final IParameter expected = action.getHeaders().get(expectedKey);

            if (expectedKey.contains("{?}"))
            {
                final String regex = expectedKey.replace("{?}", ".*");
                for (String incoming : incomingHeaders.keySet())
                {
                    if (incoming.matches(regex)) {
                        validateHeader(expectedKey, expected, incomingHeaders.get(incoming));
                    }
                }
            }
            else
            {
                final Object actual = incomingHeaders.get(expectedKey);
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
                    validateHeader(expectedKey, expected, actual);
                }
            }
        }
    }

    private void validateHeader(String expectedKey, IParameter expected, Object header) throws InvalidHeaderException {
        if (expected.isArray())
        {
            validateHeaderAsString(expectedKey, expected, getArrayHeaderAsString(header));
        }
        else if (expected.isRepeat())
        {
            for (Object h : (Iterable) header) {
                validateHeaderAsString(expectedKey, expected, getHeaderAsString(h));
            }
        }
        else
        {
            validateHeaderAsString(expectedKey, expected, getHeaderAsString(header));
        }
    }

    private void validateHeaderAsString(String expectedKey, IParameter expected, String actual) throws InvalidHeaderException {
        if (!expected.validate(actual))
        {
            final String msg = String.format("Invalid value '%s' for header %s. %s",
                                       actual, expectedKey, expected.message(actual));
            throw new InvalidHeaderException(msg);
        }
    }

    private Map<String, Object> getIncomingHeaders(MuleMessage message)
    {
        Map<String, Object> incomingHeaders = new CaseInsensitiveHashMap();
        if (message.getInboundProperty("http.headers") != null)
        {
            incomingHeaders = new CaseInsensitiveHashMap(message.<Map>getInboundProperty("http.headers"));
        }
        else
        {
            for (String key : message.getInboundPropertyNames())
            {
                if (!key.startsWith("http."))
                {
                    incomingHeaders.put(key, message.getInboundProperty(key));
                }
            }
        }
        return incomingHeaders;
    }

    /**
     *
     * Returns an expanded YAML list with each value in the given headerValue.
     *
     * @param  headerValue  an Object with the inbound property
     * @return                  the string representation of the inbound property
    */
    private String getArrayHeaderAsString(Object headerValue)
    {
        if (headerValue == null) return null;

        final Iterable properties;
        if (headerValue instanceof Iterable) {
            properties = (Iterable) headerValue;
        } else {
            properties = singletonList(headerValue);
        }

        final StringBuilder sb = new StringBuilder();
        for (Object property : properties) {
            sb.append("- ").append(String.valueOf(property)).append("\n");
        }
        return sb.toString();
    }

    private String getHeaderAsString(Object header)
    {
        if (header == null) return null;

        return String.valueOf(header);
    }

    private void setHeader(String key, String value)
    {
        requestEvent.getMessage().setProperty(key, value, PropertyScope.INBOUND);
        if (requestEvent.getMessage().getInboundProperty("http.headers") != null)
        {
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
                    validateBody(mimeTypeName);
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

    private void validateBody(String mimeTypeName) throws MuleRestException
    {
        IMimeType actionMimeType = action.getBody().get(mimeTypeName);
        boolean isJson = mimeTypeName.contains("json");
        boolean isXml = mimeTypeName.contains("xml");
        if (actionMimeType.getSchema() != null && (isXml || isJson))
        {
            if (config.isParserV2())
            {
                // json requires trimming the BOM if present
                validateSchemaV2(actionMimeType, isJson);
            }
            else
            {
                validateSchema(mimeTypeName);
            }
        }
        else if (actionMimeType.getFormParameters() != null &&
                 mimeTypeName.contains("multipart/form-data"))
        {
            validateMultipartForm(actionMimeType.getFormParameters());
        }
        else if (actionMimeType.getFormParameters() != null &&
                 mimeTypeName.contains("application/x-www-form-urlencoded"))
        {
            if (config.isParserV2())
            {
                validateUrlencodedFormV2(actionMimeType);
            }
            else
            {
                validateUrlencodedForm(actionMimeType.getFormParameters());

            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateUrlencodedForm(Map<String, List<IParameter>> formParameters) throws BadRequestException
    {
        Map<String, String> paramMap;
        try
        {
            if (requestEvent.getMessage().getPayload() instanceof Map)
            {
                paramMap = (Map<String, String>) requestEvent.getMessage().getPayload();
            }
            else
            {
                paramMap = (Map) new FormTransformer().transformMessage(requestEvent.getMessage(), requestEvent.getEncoding());
            }
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

            IParameter expected = formParameters.get(expectedKey).get(0);
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
                    throw new InvalidFormParameterException(msg);
                }
            }
        }
        requestEvent.getMessage().setPayload(paramMap);
    }

    private void validateUrlencodedFormV2(IMimeType actionMimeType) throws MuleRestException
    {
        if (!(actionMimeType instanceof MimeTypeImpl))
        {
            // validate only raml 1.0
            return;
        }
        String jsonText;
        try
        {
            Map<String, String> payload = requestEvent.getMessage().getPayload() instanceof NullPayload ? Collections.<String, String>emptyMap() : (Map<String, String>) requestEvent.getMessage().getPayload();
            jsonText = new ObjectMapper().writeValueAsString(payload);
        }
        catch (Exception e)
        {
            logger.warn("Cannot validate url-encoded form", e);
            return;
        }

        List<ValidationResult> validationResult = ((MimeTypeImpl) actionMimeType).validate(jsonText);
        if (validationResult.size() > 0)
        {
            String resultString =  "";
            for (ValidationResult result : validationResult)
            {
                resultString += result.getMessage() + "\n";
            }
            throw new InvalidFormParameterException(resultString);
        }
    }

    private void validateMultipartForm(Map<String, List<IParameter>> formParameters) throws BadRequestException
    {
        for (String expectedKey : formParameters.keySet())
        {
            if (formParameters.get(expectedKey).size() != 1)
            {
                //do not perform validation when multi-type parameters are used
                continue;
            }
            IParameter expected = formParameters.get(expectedKey).get(0);
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

    private void validateSchemaV2(IMimeType mimeType, boolean trimBom) throws BadRequestException
    {
        String payload = getPayloadAsString(requestEvent.getMessage(), trimBom);
        List<ValidationResult> validationResults;
        if (mimeType instanceof org.mule.raml.implv2.v10.model.MimeTypeImpl)
        {
            validationResults = ((org.mule.raml.implv2.v10.model.MimeTypeImpl) mimeType).validate(payload);
        }
        else
        {
            // TODO implement for 08
            // List<ValidationResult> validationResults = ((org.mule.raml.implv2.v08.model.MimeTypeImpl) mimeType).validate(payload);
            throw new RuntimeException("not supported");

        }
        if (!validationResults.isEmpty())
        {
            String message = validationResults.get(0).getMessage();
            logger.info("Schema validation failed: " + message);
            throw new BadRequestException(message);
        }
    }

    private String getPayloadAsString(MuleMessage message, boolean trimBom) throws BadRequestException
    {
        Object input = message.getPayload();
        if (input instanceof InputStream)
        {
            logger.debug("Transforming payload to perform Schema validation");
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copyLarge((InputStream) input, baos);
                byte[] bytes = baos.toByteArray();
                String encoding = getEncoding(message, bytes, logger);
                input = byteArrayToString(bytes, encoding, true);

                //update payload and encoding
                DataType<ByteArrayInputStream> dataType = DataTypeFactory.create(ByteArrayInputStream.class, message.getDataType().getMimeType());
                dataType.setEncoding(encoding);
                message.setPayload(new ByteArrayInputStream(bytes), dataType);

            }
            catch (IOException e)
            {
                throw new BadRequestException("Error processing request: " + e.getMessage());
            }
        }
        else if (input instanceof byte[])
        {
            try
            {
                String encoding = getEncoding(message, (byte[]) input, logger);
                input = byteArrayToString((byte[]) input, encoding, trimBom);

                //update message encoding
                DataType<byte[]> dataType = DataTypeFactory.create(byte[].class, message.getDataType().getMimeType());
                dataType.setEncoding(encoding);
                message.setPayload(input, dataType);
            }
            catch (IOException e)
            {
                throw new BadRequestException("Error processing request: " + e.getMessage());
            }
        }
        else if (input instanceof String)
        {
            // already in the right format
        }
        else
        {
            throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
        }
        return (String) input;
    }

    private String byteArrayToString(byte[] bytes, String charset, boolean trimBom) throws IOException
    {
        String result;
        if (trimBom)
        {
            result = IOUtils.toString(new ByteArrayInputStream(trimBom(bytes)), charset);
        }
        else
        {
            result = IOUtils.toString(bytes, charset);
        }
        return result;
    }

    private void validateSchema(String mimeTypeName) throws MuleRestException
    {
        SchemaType schemaType = mimeTypeName.contains("json") ? SchemaType.JSONSchema : SchemaType.XMLSchema;
        RestSchemaValidator validator = RestSchemaValidatorFactory.getInstance().createValidator(schemaType, requestEvent.getMuleContext());
        validator.validate(config.getName(), SchemaCacheUtils.getSchemaCacheKey(action, mimeTypeName), requestEvent, config.getApi());
    }

    private String negotiateOutputRepresentation(List<String> mimeTypes) throws MuleRestException
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
        for (String representation : mimeTypes)
        {
            if (representation.equals(bestMatch.withoutParameters().toString()))
            {
                return representation;
            }
        }
        return handleNotAcceptable();
    }

    protected String handleNotAcceptable() throws NotAcceptableException
    {
        throw new NotAcceptableException();
    }

    private List<String> getResponseMimeTypes()
    {
        List<String> mimeTypes = new ArrayList<>();
        for (Integer status : getSuccessStatusList()) {
            IResponse response = action.getResponses().get(String.valueOf(status));
            if (response != null && response.hasBody())
            {
                Map<String, IMimeType> interfacesOfTypes = response.getBody();
                for (Map.Entry<String, IMimeType> entry : interfacesOfTypes.entrySet())
                {
                    mimeTypes.add(entry.getValue().getType());
                }
                logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, mimeTypes));
            }
        }
        return mimeTypes;
    }

    protected int getSuccessStatus()
    {
        return getSuccessStatusList().get(0);
    }

    protected List<Integer> getSuccessStatusList()
    {
        List<Integer> statuses = new ArrayList<>();
        for (String status : action.getResponses().keySet())
        {
            int code = Integer.parseInt(status);
            if (code >= 200 && code < 300)
            {
                statuses.add(code);
            }
        }
        return statuses.isEmpty()? DEFAULT_SUCCESS_STATUS : statuses;
    }
}
