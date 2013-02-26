
package org.mule.module.apikit.rest.operation;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.expression.ExpressionManager;
import org.mule.module.apikit.AbstractWebServiceOperation;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.transport.NullPayload;

import com.google.common.net.MediaType;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestOperation extends AbstractWebServiceOperation implements RestOperation
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RestOperationType type;
    protected RestResource resource;
    protected Collection<RepresentationMetaData> representations = new HashSet<RepresentationMetaData>();

    @Override
    public RestOperationType getType()
    {
        return type;
    }

    public void setRepresentations(Collection<RepresentationMetaData> representations)
    {
        this.representations = representations;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        ExpressionManager expManager = request.getService().getMuleContext().getExpressionManager();

        if (accessExpression != null && !expManager.evaluateBoolean(accessExpression, request.getMuleEvent()))
        {
            throw new UnauthorizedException(this);
        }
        RepresentationMetaData responseRepresentation = null;
        if (!getRepresentations().isEmpty())
        {
            validateSupportedRequestMediaType(request);
            responseRepresentation = validateAcceptableResponeMediaType(request);
        }
        try
        {
            MuleEvent responeEvent = getHandler().process(request.getMuleEvent());

            if (responeEvent != null && responeEvent.getMessage() != null)
            {
                request.getMuleEvent().setMessage(responeEvent.getMessage());
                for (String name : responeEvent.getFlowVariableNames())
                {
                    request.getMuleEvent().setFlowVariable(name, responeEvent.getFlowVariable(name));
                }
            }
            else
            {
                request.getMuleEvent().setMessage(
                    new DefaultMuleMessage(NullPayload.getInstance(), request.getService().getMuleContext()));
            }
            if (responseRepresentation != null)
            {
                Object payload = responseRepresentation.toRepresentation(request.getMuleEvent(), request);
                request.getMuleEvent().getMessage().setPayload(payload);
            }
        }
        catch (Exception e)
        {
            throw new OperationHandlerException(e);
        }
    }

    protected void validateSupportedRequestMediaType(RestRequest request)
        throws UnsupportedMediaTypeException
    {
        boolean valid = false;
        for (RepresentationMetaData representation : representations)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing media type %s with %s\n",
                    representation.getMediaType(), request.getProtocolAdaptor().getRequestMediaType()));
            }
            if (representation.getMediaType()
                .withoutParameters()
                .is(request.getProtocolAdaptor().getRequestMediaType().withoutParameters()))
            {
                valid = true;
                break;
            }
        }
        if (!valid)
        {
            throw new UnsupportedMediaTypeException();
        }
    }

    protected RepresentationMetaData validateAcceptableResponeMediaType(RestRequest request)
        throws MediaTypeNotAcceptableException
    {
        MediaType bestMatch = RestContentTypeParser.bestMatch(representations, request.getProtocolAdaptor()
            .getAcceptableResponseMediaTypes());
        if (bestMatch == null)
        {
            throw new MediaTypeNotAcceptableException();
        }
        for (RepresentationMetaData representation : representations)
        {
            if (representation.getMediaType().equals(bestMatch))
            {
                return representation;
            }
        }
        throw new MediaTypeNotAcceptableException();
    }

    @Override
    public Collection<RepresentationMetaData> getRepresentations()
    {
        return representations;
    }

    public void setResource(RestResource resource)
    {
        this.resource = resource;
    }

}
