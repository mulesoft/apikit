
package org.mule.module.apikit.rest.resource;

import static org.mule.module.apikit.rest.action.ActionType.EXISTS;
import static org.mule.module.apikit.rest.action.ActionType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.ActionTypeNotAllowedException;
import org.mule.module.apikit.rest.action.RestAction;
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
            action = getAction(RETRIEVE);
        }
        if (action == null)
        {
            throw new ActionTypeNotAllowedException(this, actionType);
        }
        return action;
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

    protected MuleEvent processResource(RestRequest restRequest) throws RestException
    {
        try
        {
            this.getAction(restRequest.getProtocolAdaptor().getActionType(), restRequest.getMuleEvent())
                .handle(restRequest);
            if (ActionType.EXISTS == restRequest.getProtocolAdaptor().getActionType())
            {
                restRequest.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
            }
        }
        catch (RestException rana)
        {
            restRequest.getProtocolAdaptor().handleException(rana, restRequest.getMuleEvent());
        }
        return restRequest.getMuleEvent();
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

    protected abstract Set<ActionType> getSupportedActionTypes();

}
