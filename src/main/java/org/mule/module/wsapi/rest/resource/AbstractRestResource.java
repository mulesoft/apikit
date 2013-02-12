
package org.mule.module.wsapi.rest.resource;

import static org.mule.module.wsapi.rest.action.ActionType.EXISTS;
import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.rest.RestRequest;
import org.mule.module.wsapi.rest.RestResourceRouter;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;
import org.mule.transport.NullPayload;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestResource extends RestResourceRouter implements RestResource
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String name;
    protected String templateUri;
    protected List<RestAction> actions;

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getTemplateUri()
    {
        if (this.templateUri != null)
        {
            return this.templateUri;
        }
        return getName();
    }

    public void setTemplateUri(String templateUri)
    {
        this.templateUri = templateUri;
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

    @Override
    public RestAction getAction(ActionType actionType, MuleEvent muleEvent)
        throws RestActionNotAllowedException
    {
        if (!isActionSupported(actionType))
        {
            throw new RestActionNotAllowedException(String.format(
                "Action %s not supported by resource %s of type %s", actionType, getTemplateUri(),
                getClass().getSimpleName()), muleEvent);
        }
        RestAction action = getAction(actionType);
        if (action == null && EXISTS == actionType)
        {
            action = getAction(RETRIEVE);
        }
        if (action == null)
        {
            throw new RestActionNotAllowedException(String.format(
                "Action %s supported but not defined by resource %s of type %s", actionType,
                getTemplateUri(), getClass().getSimpleName()), muleEvent);
        }
        return action;
    }

    @Override
    public boolean isActionSupported(ActionType actionType)
    {
        return getSupportedActions().contains(actionType);
    }

    @Override
    public final MuleEvent handle(RestRequest restCall) throws MuleException
    {
        if (restCall.hasMorePathElements())
        {
            return super.handle(restCall);
        }
        else
        {
            return processResource(restCall);
        }
    }

    protected MuleEvent processResource(RestRequest restCall) throws MuleException
    {
        try
        {
            this.getAction(restCall.getProtocolAdaptor().getActionType(), restCall.getMuleEvent()).handle(
                restCall);
            if (ActionType.EXISTS == restCall.getProtocolAdaptor().getActionType())
            {
                restCall.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
            }
        }
        catch (RestActionNotAllowedException rana)
        {
            restCall.getProtocolAdaptor().statusActionNotAllowed(restCall.getMuleEvent());
        }
        return restCall.getMuleEvent();
    }

}
