package org.mule.module.apikit;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.transform.TransformerCache;
import org.mule.module.apikit.validation.RestSchemaValidator;
import org.mule.module.apikit.validation.RestSchemaValidatorFactory;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestRequest
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MuleEvent requestEvent;
    private Raml api;
    private Action action;
    private HttpProtocolAdapter adapter;

    public HttpRestRequest(MuleEvent event, Raml api)
    {
        requestEvent = event;
        this.api = api;
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

        //process query parameters
        processQueryParameters();

        //process header parameters

        //validate request representation (content-type and schema if defined)
        validateInputRepresentation();

        //negotiate output representation
        String responseRepresentation = negotiateOutputRepresentation();

        //normalize payload

        MuleEvent responseEvent = flow.process(requestEvent);

        //transform response
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

        //hateoas enricher

        return responseEvent;
    }

    private void processQueryParameters() throws InvalidQueryParameterException
    {
        for (String expectedKey : action.getQueryParameters().keySet())
        {
            QueryParameter expected = action.getQueryParameters().get(expectedKey);
            String actual = requestEvent.getMessage().getInboundProperty(expectedKey);
            if (actual == null && expected.isRequired())
            {
                throw new InvalidQueryParameterException("Required query parameter " + expectedKey + " not specified");
            }
            if (actual != null)
            {
                if (!expected.validate(actual))
                {
                    throw new InvalidQueryParameterException("Invalid uri parameter value " + actual + " for " + expectedKey);
                }
            }
        }
    }

    private void transformToExpectedContentType(MuleEvent muleEvent, String responseRepresentation) throws MuleException
    {
        MuleMessage message = muleEvent.getMessage();
        String msgMimeType = message.getDataType() != null ? message.getDataType().getMimeType() : null;
        String msgContentType = message.getOutboundProperty("Content-Type");
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

    private void validateInputRepresentation() throws MuleRestException
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
                if (action.getBody().get(mimeTypeName).getSchema() != null &&
                    (mimeTypeName.contains("xml") || mimeTypeName.contains("json")))
                {
                    validateSchema(mimeTypeName);
                }
                break;
            }
        }
        if (!found)
        {
            throw new UnsupportedMediaTypeException();
        }
    }

    private void validateSchema(String mimeTypeName) throws MuleRestException
    {
        SchemaType schemaType = mimeTypeName.contains("json") ? SchemaType.JSONSchema : SchemaType.XMLSchema;
        RestSchemaValidator validator = RestSchemaValidatorFactory.getInstance().createValidator(schemaType, requestEvent.getMuleContext());
        StringBuilder key = new StringBuilder(action.getResource().getUri());
        key.append(",").append(action.getType());
        key.append(",").append(mimeTypeName);
        validator.validate(key.toString(), requestEvent, api);

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
        return -1;
    }
}
