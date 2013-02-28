
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
import org.mule.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.net.MediaType;

import java.io.IOException;
import java.util.ArrayList;
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
        if (!getAllRepresentations().isEmpty())
        {
            if (getType().isRequestExpected())
            {
                validateSupportedRequestMediaType(request);
            }
            if (getType().isResponseExpected())
            {
                responseRepresentation = validateAcceptableResponeMediaType(request);
            }
        }
        try
        {
            MuleEvent responseEvent = getHandler().process(request.getMuleEvent());

            if (responseEvent != null && responseEvent.getMessage() != null)
            {
                if (responseEvent.getMessage().getExceptionPayload() != null)
                {
                    throw new OperationHandlerException(responseEvent.getMessage()
                        .getExceptionPayload()
                        .getException());
                }
                else
                {
                    request.getMuleEvent().setMessage(responseEvent.getMessage());
                    for (String name : responseEvent.getFlowVariableNames())
                    {
                        request.getMuleEvent().setFlowVariable(name, responseEvent.getFlowVariable(name));
                    }
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
        MediaType requestMediaType = request.getProtocolAdaptor().getRequestMediaType();
        if (requestMediaType == null)
        {
            // if request Content-Type is not sent, skip validation
            return;
        }

        boolean valid = false;
        for (RepresentationMetaData representation : getAllRepresentations())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing media type %s with %s\n",
                    representation.getMediaType(), requestMediaType));
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
        MediaType bestMatch = RestContentTypeParser.bestMatch(getAllRepresentations(),
            request.getProtocolAdaptor().getAcceptableResponseMediaTypes());
        if (bestMatch == null)
        {
            throw new MediaTypeNotAcceptableException();
        }
        for (RepresentationMetaData representation : getAllRepresentations())
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

    public Collection<RepresentationMetaData> getAllRepresentations()
    {
        Collection<RepresentationMetaData> allRepresentations = new ArrayList<RepresentationMetaData>();
        allRepresentations.addAll(resource.getRepresentations());
        allRepresentations.addAll(getRepresentations());
        return allRepresentations;
    }

    @Override
    public void appendSwaggerDescriptor(JsonGenerator jsonGenerator)
        throws JsonGenerationException, IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDescription()
    {
        if (!StringUtils.isEmpty(super.getDescription()))
        {
            return super.getDescription();
        }
        else
        {
            return type.name() + " " + resource.getName();
        }
    }

}
