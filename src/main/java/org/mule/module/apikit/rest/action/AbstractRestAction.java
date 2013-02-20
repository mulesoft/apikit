
package org.mule.module.apikit.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.expression.ExpressionManager;
import org.mule.module.apikit.AbstractWebServiceOperation;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.representation.Representation;
import org.mule.module.apikit.rest.util.RestContentTypeParser;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractRestAction extends AbstractWebServiceOperation implements RestAction
{

    protected ActionType type;
    protected Collection<Representation> representations = new HashSet<Representation>();

    @Override
    public ActionType getType()
    {
        return type;
    }

    public void setRepresentations(Collection<Representation> representations)
    {
        this.representations = representations;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        ExpressionManager expManager = request.getMuleEvent().getMuleContext().getExpressionManager();

        if (accessExpression != null && !expManager.evaluateBoolean(accessExpression, request.getMuleEvent()))
        {
            throw new UnauthorizedException(this);
        }
        if (!getRepresentations().isEmpty())
        {
            validateSupportedRequestMediaType(request);
            validateAcceptableResponeMediaType(request);
        }
        try
        {
            return getHandler().process(request.getMuleEvent());
        }
        catch (MuleException e)
        {
            throw new RestException();
        }
    }

    protected void validateSupportedRequestMediaType(RestRequest request) throws UnsupportedMediaTypeException
    {
        boolean valid = false;
        for (Representation representation : representations)
        {
            //TODO maybe a smarter comparison is required
            if (representation.getMediaType().equals(request.getProtocolAdaptor().getRequestContentType()))
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

    protected void validateAcceptableResponeMediaType(RestRequest request)
        throws MediaTypeNotAcceptableException
    {
        String bestMatch = RestContentTypeParser.bestMatch(representations, request.getProtocolAdaptor().getAcceptedContentTypes());
        if (bestMatch == null)
        {
            throw new MediaTypeNotAcceptableException();
        }
    }

    @Override
    public Collection<Representation> getRepresentations()
    {
        return representations;
    }
}
