/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowFinder
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, IResource> flatResourceTree = new HashMap<>();
    private Map<String, Flow> restFlowMap;
    private Map<String, Flow> restFlowMapUnwrapped;

    protected RoutingTable routingTable;

    private RamlHandler ramlHandler;
    private String configName;
    private List<FlowMapping> flowMappings;
    private MuleContext muleContext;

    public FlowFinder(RamlHandler ramlHandler, String configName, MuleContext muleContext, List<FlowMapping> flowMappings)
    {
        this.muleContext = muleContext;
        this.ramlHandler = ramlHandler;
        this.configName = configName;
        this.flowMappings = flowMappings;
        initializeRestFlowMap();
        loadRoutingTable();
    }

    protected void initializeRestFlowMap()
    {
        flattenResourceTree(ramlHandler.getApi().getResources());

        if (restFlowMap == null)
        {
            restFlowMap = new HashMap<>();

            //init flows by convention
            Collection<Flow> flows = muleContext.getRegistry().lookupObjects(Flow.class);



            for (Flow flow : flows)
            {
                String key = getRestFlowKey(flow.getName());
                if (key != null)
                {
                    restFlowMap.put(key, flow);
                }
            }

            ////init flow mappings
            for (FlowMapping mapping : flowMappings)
            {
                for (Flow flow : flows)
                {
                    if (flow.getName().equals(mapping.getFlowRef()))
                    {
                        mapping.setFlow(flow);
                        restFlowMap.put(mapping.getKey(), mapping.getFlow());
                    }
                }
            }

            logMissingMappings();

            restFlowMapUnwrapped = new HashMap<>(restFlowMap);
        }
    }

    private void flattenResourceTree(Map<String, IResource> resources)
    {
        for (IResource resource : resources.values())
        {
            flatResourceTree.put(resource.getUri(), resource);
            if (resource.getResources() != null)
            {
                flattenResourceTree(resource.getResources());
            }
        }
    }

    public Map<String, Flow> getRawRestFlowMap()
    {
        return restFlowMap;
    }

    /**
     * validates if name is a valid router flow name according to the following pattern:
     *  method:/resource[:content-type][:config-name]
     *
     * @param name to be validated
     * @return the name with the config-name stripped or null if it is not a router flow
     */
    private String getRestFlowKey(String name)
    {
        String[] coords = name.split(":");
        String[] methods = {"get", "put", "post", "delete", "head", "patch", "options"};
        if (coords.length < 2 || coords.length > 4 ||
            !Arrays.asList(methods).contains(coords[0]) ||
            !coords[1].startsWith("/"))
        {
            return null;
        }
        if (coords.length == 4)
        {
            if (coords[3].equals(configName))
            {
                return validateRestFlowKeyAgainstApi(coords[0], coords[1], coords[2]);
            }
            return null;
        }
        if (coords.length == 3)
        {
            if (coords[2].equals(configName))
            {
                return validateRestFlowKeyAgainstApi(coords[0], coords[1]);
            }
            return validateRestFlowKeyAgainstApi(coords[0], coords[1], coords[2]);
        }
        return validateRestFlowKeyAgainstApi(coords[0], coords[1]);
    }

    private String validateRestFlowKeyAgainstApi(String... coords)
    {
        String method = coords[0];
        String resource = coords[1];
        String type = coords.length == 3 ? coords[2] : null;
        String key = String.format("%s:%s", method, resource);
        if (type != null)
        {
            key = key + ":" + type;
        }
        IResource apiResource = flatResourceTree.get(resource);
        if (apiResource != null)
        {
            IAction action = apiResource.getAction(method);
            if (action != null)
            {
                if (type == null)
                {
                    return key;
                }
                else
                {
                    if (action.hasBody() && action.getBody().get(type) != null)
                    {
                        return key;
                    }
                }
            }
        }
        logger.warn(String.format("Flow named \"%s\" does not match any RAML descriptor resource", key));
        return null;
    }

    private void logMissingMappings()
    {
        for (IResource resource : flatResourceTree.values())
        {
            String fullResource = resource.getUri();
            for (IAction action : resource.getActions().values())
            {
                String method = action.getType().name().toLowerCase();
                String key = method + ":" + fullResource;
                if (restFlowMap.get(key) != null)
                {
                    continue;
                }
                if (action.hasBody())
                {
                    for (String contentType : action.getBody().keySet())
                    {
                        if (restFlowMap.get(key + ":" + contentType) == null)
                        {
                            logger.warn(String.format("Action-Resource-ContentType triplet has no implementation -> %s:%s:%s ",
                                                      method, fullResource, contentType));
                        }
                    }
                }
                else
                {
                    logger.warn(String.format("Action-Resource pair has no implementation -> %s:%s ",
                                              method, fullResource));
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("==== RestFlows defined:");
            for (String key : restFlowMap.keySet())
            {
                logger.debug("\t\t" + key);
            }
        }
    }

    private void loadRoutingTable() {
        if (routingTable == null) {
            routingTable = new RoutingTable(ramlHandler.getApi());
        }

    }


    public Flow getFlow(IResource resource, String method, String contentType) throws UnsupportedMediaTypeException
    {
        String baseKey = method + ":" + resource.getUri();
        Map<String, Flow> rawRestFlowMap = getRawRestFlowMap();
        Flow flow = rawRestFlowMap.get(baseKey + ":" + contentType);
        if (flow == null)
        {
            flow = rawRestFlowMap.get(baseKey);
            if (flow == null && isFlowDeclaredWithDifferentMediaType(rawRestFlowMap, baseKey))
            {
                //throw new UnsupportedMediaTypeException();
                throw ApikitErrorTypes.throwErrorTypeNew(new UnsupportedMediaTypeException());
            }
        }
        return flow;
    }

    public IResource getResource(URIPattern uriPattern)
    {
        return routingTable.getResource(uriPattern);
    }

    private boolean isFlowDeclaredWithDifferentMediaType(Map<String, Flow> map, String baseKey)
    {
        for (String flowName : map.keySet())
        {
            String [] split = flowName.split(":");
            String methodAndResoruce = split[0] + ":" + split[1];
            if (methodAndResoruce.equals(baseKey))
                return true;
        }
        return false;
    }

    public URIPattern findBestMatch(URIResolver resolver)
    {
        return resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH);
    }
}
