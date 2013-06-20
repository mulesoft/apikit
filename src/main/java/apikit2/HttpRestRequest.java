package apikit2;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.transform.DataTypePair;
import org.mule.module.apikit.rest.transform.TransformerCache;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.transformer.types.DataTypeFactory;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import heaven.model.Action;
import heaven.model.Body;
import heaven.model.Heaven;
import heaven.model.MimeType;
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
        MuleEvent responseEvent = requestEvent;
        String responseRepresentation;
        try
        {

            //process query parameters

            //validate request representation (content-type and schema if defined)
            validateInputRepresentation();

            //negotiate output representation
            responseRepresentation = negotiateOutputRepresentation();

            //normalize payload

            responseEvent = flow.process(requestEvent);

            //build location response

            //transform response
            if (responseRepresentation != null)
            {
                transformToExpectedContentType(responseEvent, responseRepresentation);
            }

            //hateoas enricher

            return responseEvent;
        }
        catch (RestException e)
        {
            //TODO handle error response
            throw new MessagingException(responseEvent, e);
        }
    }

    private void transformToExpectedContentType(MuleEvent muleEvent, String responseRepresentation) throws RestException
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
            throw new RestException(e);
        }

        muleEvent.getMessage().setOutboundProperty("Content-Type", responseRepresentation);
    }

    private void validateInputRepresentation() throws RestException
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
            if (mimeTypeName.equals(requestMimeTypeName))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(String.format("comparing request media type %s with expected %s\n",
                                        requestMimeTypeName, mimeTypeName));
                }
                found = true;
                //TODO validate schema if defined
                break;
            }
        }
        if (!found)
        {
            throw new UnsupportedMediaTypeException();
        }
    }

    private String negotiateOutputRepresentation() throws RestException
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
            throw new MediaTypeNotAcceptableException();
        }
        logger.debug("=== negotiated response content-type: " + bestMatch.toString());
        for (MimeType representation : mimeTypes)
        {
            if (representation.getType().equals(bestMatch.withoutParameters().toString()))
            {
                return representation.getType();
            }
        }
        throw new MediaTypeNotAcceptableException();
    }

    private List<MimeType> getResponseMimeTypes()
    {
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        for (Integer status : action.getResponses().keySet())
        {
            if (status >= 200 && status < 300)
            {
                Body body = action.getResponses().get(status);
                if (body != null)
                {
                    Collection<MimeType> types = body.getMimeTypes().values();
                    logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, types));
                    mimeTypes.addAll(types);
                }
            }
        }
        return mimeTypes;
    }

}
