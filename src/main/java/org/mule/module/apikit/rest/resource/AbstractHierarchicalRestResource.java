
package org.mule.module.apikit.rest.resource;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

public abstract class AbstractHierarchicalRestResource extends AbstractRestResource
    implements HierarchicalRestResource
{
    protected List<RestResource> resources = Collections.unmodifiableList(new ArrayList<RestResource>());
    protected Map<String, RestResource> routingTable = new HashMap<String, RestResource>();

    public AbstractHierarchicalRestResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
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
            restRequest.getProtocolAdaptor().handleException(re, restRequest);

        }
    }

    public void buildRoutingTable() throws InitialisationException
    {
        routingTable = new HashMap<String, RestResource>();
        if (getResources() == null)
        {
            return;
        }
        for (RestResource resource : getResources())
        {
            String uriPattern = resource.getName();
            if (routingTable.containsKey(uriPattern))
            {
                throw new InitialisationException(
                    MessageFactory.createStaticMessage("Duplicate resource name: " + uriPattern), this);
            }
            logger.debug("Adding URI to the routing table: " + uriPattern);
            routingTable.put(uriPattern, resource);
        }
    }

    public List<RestResource> getResources()
    {
        return resources;
    }

    @Override
    public List<RestResource> getAuthorizedResources(RestRequest request)
    {
        List<RestResource> result = new ArrayList<RestResource>();
        for (RestResource resource : getResources())
        {
            if (isAuthorized(resource, request))
            {
                result.add(resource);
            }
        }
        return result;
    }

    public void setResources(List<RestResource> resources)
    {
        this.resources = Collections.unmodifiableList(resources);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        buildRoutingTable();
    }

    @Override
    public void appendSwaggerJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException
    {
        super.appendSwaggerJson(jsonGenerator);
        for (RestResource resource : getResources())
        {
            resource.appendSwaggerJson(jsonGenerator);
        }
    }
}
