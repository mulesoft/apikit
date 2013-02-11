
package org.mule.module.wsapi.rest.resource;

import static org.mule.module.wsapi.rest.action.ActionType.EXISTS;
import static org.mule.module.wsapi.rest.action.ActionType.RETRIEVE;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.api.WebServiceRoute;
import org.mule.module.wsapi.rest.RestRequest;
import org.mule.module.wsapi.rest.RestWebServiceInterface;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapter;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapterFactory;
import org.mule.transport.NullPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestResource implements RestResource
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RestWebServiceInterface restInterface;
    protected String name;
    protected List<? extends WebServiceRoute> routes;
    protected String templateUri;
    protected List<RestAction> actions;
    protected Map<String, RestResource> routingTable;

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

    @Override
    public List<RestResource> getResources()
    {
        return (List<RestResource>) getRoutes();
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
    public MessageProcessor getAction(ActionType actionType, MuleEvent muleEvent)
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
    public void buildRoutingTable()
    {
        routingTable = new HashMap<String, RestResource>();
        if (getResources() == null)
        {
            return;
        }
        for (RestResource resource : getResources())
        {
            String uriPattern = resource.getTemplateUri();
            logger.debug("Adding URI to the routing table: " + uriPattern);
            routingTable.put(uriPattern, resource);
            resource.buildRoutingTable();
        }
    }

    @Override
    public List<? extends WebServiceRoute> getRoutes()
    {
        return routes;
    }

    public void setRoutes(List<? extends WebServiceRoute> routes)
    {
        this.routes = routes;
    }
    
    @Override
    public MuleEvent process(RestRequest restCall) throws MuleException
    {
        RestProtocolAdapter protocolAdapter = restCall.getProtocolAdaptor();
        if (protocolAdapter.hasMorePathElements())
        {
            RestResource resource = routingTable.get(protocolAdapter.getNextPathElement());
            if (resource != null)
            {
                resource.process(restCall);
            }
            else
            {
                protocolAdapter.statusResourceNotFound(restCall.getMuleEvent());
            }
        }
        else
        {
            try
            {
                this.getAction(protocolAdapter.getActionType(), restCall.getMuleEvent()).process(restCall.getMuleEvent());
                if (ActionType.EXISTS == protocolAdapter.getActionType())
                {
                    restCall.getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
                }
            }
            catch (RestActionNotAllowedException rana)
            {
                protocolAdapter.statusActionNotAllowed(restCall.getMuleEvent());
            }
        }
        return restCall.getMuleEvent();

    }

    protected RestProtocolAdapter getProtocolAdapter(MuleEvent muleEvent)
    {
        return RestProtocolAdapterFactory.getInstance().getAdapterForEvent(muleEvent);
    }

}
