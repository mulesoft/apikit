package apikit2;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.module.apikit.rest.representation.SchemaType;
import org.mule.module.apikit.rest.transform.DataTypePair;
import org.mule.module.apikit.rest.transform.TransformerCache;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.module.apikit.rest.validation.RestSchemaValidator;
import org.mule.module.apikit.rest.validation.RestSchemaValidatorFactory;
import org.mule.transformer.types.DataTypeFactory;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import apikit2.exception.ApikitRuntimeException;
import apikit2.exception.InvalidQueryParameterException;
import apikit2.exception.MuleRestException;
import apikit2.exception.NotAcceptableException;
import apikit2.exception.UnsupportedMediaTypeException;
import heaven.model.Action;
import heaven.model.Body;
import heaven.model.Heaven;
import heaven.model.MimeType;
import heaven.model.parameter.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestRequest
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MuleEvent requestEvent;
    private Heaven api;
    private Action action;
    private HttpProtocolAdapter adapter;

    public HttpRestRequest(MuleEvent event, Heaven api)
    {
        requestEvent = event;
        this.api = api;
        adapter = new HttpProtocolAdapter(event);
    }

    public String getResourcePath()
    {
        return adapter.getResourceURI().getPath();
    }

    public String getMethod()
    {
        return adapter.getMethod().toLowerCase();
    }

    public MuleEvent process(RestFlow flow, Action action) throws MuleException
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
        for (QueryParameter expected : action.getQueryParameters().values())
        {
            String actual = requestEvent.getMessage().getInboundProperty(expected.getName());
            if (actual == null && expected.isRequired())
            {
                throw new InvalidQueryParameterException("Required query parameter " + expected.getName() + " not specified");
            }
            if (actual != null)
            {
                if (!expected.validate(actual))
                {
                    throw new InvalidQueryParameterException("Invalid uri parameter value " + actual + " for " + expected.getName());
                }
            }
        }
    }

    private void transformToExpectedContentType(MuleEvent muleEvent, String responseRepresentation) throws MuleException
    {
        DataType sourceDataType = DataTypeFactory.create(muleEvent.getMessage().getPayload().getClass());
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
            Object payload = transformer.transform(muleEvent.getMessage().getPayload());
            muleEvent.getMessage().setPayload(payload);
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(e);
        }

        muleEvent.getMessage().setOutboundProperty("Content-Type", responseRepresentation);
    }

    private void validateInputRepresentation() throws MuleRestException
    {
        if (action == null || action.getBody() == null)
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
        for (String mimeTypeName : action.getBody().getMimeTypes().keySet())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing request media type %s with expected %s\n",
                                           requestMimeTypeName, mimeTypeName));
            }
            if (mimeTypeName.equals(requestMimeTypeName))
            {
                found = true;
                //TODO validate schema if defined
                if (action.getBody().getMimeTypes().get(mimeTypeName).getSchema() != null &&
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
        key.append(",").append(action.getName());
        key.append(",").append(mimeTypeName);
        validator.validate(key.toString(), requestEvent, api);

    }

    private String negotiateOutputRepresentation() throws MuleRestException
    {
        List<MimeType> mimeTypes = getResponseMimeTypes();
        if (action == null || action.getResponses() == null || mimeTypes.isEmpty())
        {
            //no response media-types defined, return highest quality accept type
            return adapter.getAcceptableResponseMediaTypes().split(",")[0];
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
            Body body = action.getResponses().get(status);
            if (body != null)
            {
                Collection<MimeType> types = body.getMimeTypes().values();
                logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, types));
                mimeTypes.addAll(types);
            }
        }
        return mimeTypes;
    }

    private int getSuccessStatus()
    {
        for (Integer status : action.getResponses().keySet())
        {
            if (status >= 200 && status < 300)
            {
                return status;
            }
        }
        return -1;
    }
}
