/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.i18n.MessageFactory;
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

}
