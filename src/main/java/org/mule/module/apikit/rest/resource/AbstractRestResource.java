
package org.mule.module.apikit.rest.resource;

import static org.mule.module.apikit.rest.action.ActionType.EXISTS;
import static org.mule.module.apikit.rest.action.ActionType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.expression.ExpressionManager;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.ActionTypeNotAllowedException;
import org.mule.module.apikit.rest.action.RestAction;
import org.mule.module.apikit.rest.action.RestExistsByRetrieveAction;
import org.mule.transport.NullPayload;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestResource implements RestResource
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String name;
    protected String description = "";
    protected List<RestAction> actions = new ArrayList<RestAction>();
    protected String accessExpression;

    public AbstractRestResource(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<RestAction> getActions()
    {
        return actions;
    }

    public void setActions(List<RestAction> actions)
    {
        this.actions = actions;
    }

    private RestAction getAction(ActionType actionType)
    {
        RestAction action = null;
        for (RestAction a : getActions())
        {
            if (a.getType() == actionType)
            {
                action = a;
                break;
            }
        }
        return action;
    }

    protected RestAction getAction(ActionType actionType, MuleEvent muleEvent)
        throws ActionTypeNotAllowedException
    {
        if (!getSupportedActionTypes().contains(actionType))
        {
            throw new ActionTypeNotAllowedException(this, actionType);
        }
        RestAction action = getAction(actionType);
        if (action == null && EXISTS == actionType)
        {
            action = useRetrieveAsExists();
        }
        if (action == null)
        {
            throw new ActionTypeNotAllowedException(this, actionType);
        }
        return action;
    }

    private RestAction useRetrieveAsExists()
    {
        RestAction retrieve = getAction(RETRIEVE);
        if (retrieve == null)
        {
            return null;
        }
        return new RestExistsByRetrieveAction(retrieve);
    }

    @Override
    public boolean isActionTypeAllowed(ActionType actionType)
    {
        for (RestAction action : actions)
        {
            if (action.getType().equals(actionType))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<ActionType> getAllowedActionTypes()
    {
        Set<ActionType> allowedTypes = new HashSet<ActionType>();
        for (RestAction action : actions)
        {
            allowedTypes.add(action.getType());
        }
        return allowedTypes;
    }

    @Override
    public MuleEvent handle(RestRequest restCall) throws RestException
    {
        return processResource(restCall);
    }

    protected MuleEvent processResource(RestRequest request) throws RestException
    {
        try
        {
            authorize(request);
            this.getAction(request.getProtocolAdaptor().getActionType(), request.getMuleEvent()).handle(
                request);
            if (ActionType.EXISTS == request.getProtocolAdaptor().getActionType())
            {
                request.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
            }
        }
        catch (RestException rana)
        {
            request.getProtocolAdaptor().handleException(rana, request.getMuleEvent());
        }
        return request.getMuleEvent();
    }

    @Override
    public String getAccessExpression()
    {
        return accessExpression;
    }

    public void setAccessExpression(String accessExpression)
    {
        this.accessExpression = accessExpression;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public List<RestAction> getAuthorizedActions(RestRequest request)
    {
        List<RestAction> result = new ArrayList<RestAction>();
        for (RestAction action : getActions())
        {
            if (isAuthorized(action, request))
            {
                result.add(action);
            }
        }
        return result;
    }

    protected void authorize(RestRequest request) throws UnauthorizedException
    {
        if (!isAuthorized(this, request))
        {
            throw new UnauthorizedException(this);
        }
    }

    protected boolean isAuthorized(WebServiceRoute route, RestRequest request)
    {
        ExpressionManager expManager = request.getMuleEvent().getMuleContext().getExpressionManager();

        if (route.getAccessExpression() == null
            || expManager.evaluateBoolean(route.getAccessExpression(), request.getMuleEvent()))
        {
            return true;
        }
        return false;
    }

    protected abstract Set<ActionType> getSupportedActionTypes();

}
