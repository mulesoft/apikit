
package org.mule.module.wsapi.rest;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.wsapi.rest.resource.RestResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestResourceRouter implements RestRequestHandler
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected List<RestResource> resources = new ArrayList<RestResource>();
    protected Map<String, RestResource> routingTable;

    @Override
    public MuleEvent handle(RestRequest restCall) throws MuleException
    {
        if (routingTable == null)
        {
            buildRoutingTable();
        }
        RestResource resource = routingTable.get(restCall.getNextPathElement());
        if (resource != null)
        {
            return resource.handle(restCall);
        }
        else
        {
            restCall.getProtocolAdaptor().statusResourceNotFound(restCall.getMuleEvent());
            return restCall.getMuleEvent();
        }
    }

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

    public List<RestResource> getResources()
    {
        return resources;
    }

    public void setResources(List<RestResource> resources)
    {
        this.resources = resources;
    }

}
