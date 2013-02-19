
package org.mule.module.apikit.rest.action;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.expression.ExpressionManager;
import org.mule.module.apikit.AbstractWebServiceOperation;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.api.Representation;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.util.RestContentTypeParser;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractRestAction extends AbstractWebServiceOperation implements RestAction
{

    protected ActionType type;

    @Override
    public ActionType getType()
    {
        return type;
    }

    public void setRepresentation(Representation representation)
    {
        this.representation = representation;
    }

    @Override
    public MuleEvent handle(RestRequest request) throws RestException
    {
        ExpressionManager expManager = request.getMuleEvent().getMuleContext().getExpressionManager();

        if (accessExpression != null && !expManager.evaluateBoolean(accessExpression, request.getMuleEvent()))
        {
            throw new UnauthorizedException(this);
        }
        if (getRepresentation() != null)
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

    protected void validateSupportedRequestMediaType(RestRequest request)
        throws UnsupportedMediaTypeException
    {
        Collection<Representation> representations = Collections.singletonList(getRepresentation());

        if (!representations.contains(request.getProtocolAdaptor().getRequestContentType()))
        {
            throw new UnsupportedMediaTypeException();
        }
    }

    protected void validateAcceptableResponeMediaType(RestRequest request)
        throws MediaTypeNotAcceptableException
    {
        Collection<Representation> representations = Collections.singletonList(getRepresentation());
        String bestMatch = RestContentTypeParser.bestMatch(representations, request.getProtocolAdaptor()
            .getAcceptedContentTypes());
        if (bestMatch == null)
        {
            throw new MediaTypeNotAcceptableException();
        }
    }

}
