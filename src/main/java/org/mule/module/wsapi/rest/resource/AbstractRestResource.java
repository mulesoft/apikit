
package org.mule.module.wsapi.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;

import java.util.List;

public abstract class AbstractRestResource implements RestResource
{
    protected String name;
    protected List<RestResource> resources;
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
            return getParentTemplateUri() + "/" + this.templateUri;
        }
        return getParentTemplateUri() + "/" + getName();
        //return getParent().getTemplateUri() + "/{" + pathParameter.getName() + "}";
    }

    private String getParentTemplateUri()
    {
        return "/api";  //TODO
    }

    public void setTemplateUri(String templateUri)
    {
        this.templateUri = templateUri;
    }

    @Override
    public List<RestResource> getResources()
    {
        return resources;
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
    public MessageProcessor getAction(ActionType actionType, MuleEvent muleEvent) throws RestActionNotAllowedException
    {
        if (!isActionSupported(actionType))
        {
            throw new RestActionNotAllowedException(String.format("Action %s not supported by resource %s of type %s", actionType, getTemplateUri(), getClass().getSimpleName()), muleEvent);
        }
        RestAction action = getAction(actionType);
        if (action == null)
        {
            throw new RestActionNotAllowedException(String.format("Action %s supported but not defined by resource %s of type %s", actionType, getTemplateUri(), getClass().getSimpleName()), muleEvent);
        }
        return action;
    }

    @Override
    public boolean isActionSupported(ActionType actionType)
    {
        return getSupportedActions().contains(actionType);
    }
}
