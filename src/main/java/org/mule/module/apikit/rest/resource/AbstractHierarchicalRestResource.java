
package org.mule.module.apikit.rest.resource;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractHierarchicalRestResource extends AbstractRestResource
    implements HierarchicalRestResource
{
    protected List<RestResource> resources = Collections.unmodifiableList(new ArrayList<RestResource>());
    protected Map<String, RestResource> routingTable = new HashMap<String, RestResource>();

    public AbstractHierarchicalRestResource(String name)
    {
        super(name);
    }

    @Override
    public MuleEvent handle(RestRequest restRequest) throws RestException
    {
        try
        {
            if (restRequest.hasMorePathElements())
            {
                String path = restRequest.getNextPathElement();
                RestResource resource = routingTable.get(path);
                if (resource != null)
                {
                    resource.handle(restRequest);
                }
                else
                {
                    throw new ResourceNotFoundException(path);
                }
            }
            else
            {
                processResource(restRequest);
            }
        }
        catch (RestException re)
        {
            restRequest.getProtocolAdaptor().handleException(re, restRequest.getMuleEvent());

        }
        return restRequest.getMuleEvent();
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
            String uriPattern = resource.getName();
            logger.debug("Adding URI to the routing table: " + uriPattern);
            routingTable.put(uriPattern, resource);
        }
    }

    public List<RestResource> getResources()
    {
        return resources;
    }

    public void setResources(List<RestResource> resources)
    {
        this.resources = Collections.unmodifiableList(resources);
        buildRoutingTable();
    }

}
