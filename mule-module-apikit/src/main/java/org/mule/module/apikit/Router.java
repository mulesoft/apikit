/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.Configuration.APPLICATION_RAML;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.StartException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.InvalidUriParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.transport.http.HttpConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router implements MessageProcessor, Startable, MuleContextAware, FlowConstructAware
{

    private static final int URI_CACHE_SIZE = 1000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MuleContext muleContext;
    private FlowConstruct flowConstruct;
    private Configuration config;
    private Map<String, Flow> restFlowMap;
    private Map<URIPattern, Resource> routingTable;
    private LoadingCache<String, URIResolver> uriResolverCache;
    private LoadingCache<String, URIPattern> uriPatternCache;
    private ConsoleHandler consoleHandler;

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    public Configuration getConfig()
    {
        return config;
    }

    private Raml getApi()
    {
        return getConfig().getApi();
    }

    private String getRaml(String host)
    {
        return getConfig().getApikitRaml(host);
    }

    @Override
    public void start() throws MuleException
    {
        //avoid spring initialization
        if (flowConstruct == null)
        {
            return;
        }
        if (config == null)
        {
            try
            {
                config = muleContext.getRegistry().lookupObject(Configuration.class);
            }
            catch (RegistrationException e)
            {
                throw new StartException(MessageFactory.createStaticMessage("APIKit configuration not Found"), this);
            }
        }

        loadRestFlowMap();
        config.loadApiDefinition(muleContext, flowConstruct, restFlowMap);
        if (config.isConsoleEnabled())
        {
            consoleHandler = new ConsoleHandler(getApi().getBaseUri(), config.getConsolePath());
            consoleHandler.publishConsoleUrl(muleContext.getConfiguration().getWorkingDirectory());
        }

        routingTable = new HashMap<URIPattern, Resource>();
        buildRoutingTable(getApi().getResources());

        logger.info("Building resource URI cache...");
        uriResolverCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIResolver>()
                        {
                            public URIResolver load(String path) throws IOException
                            {
                                return new URIResolver(path);
                            }
                        });

        uriPatternCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIPattern>()
                        {
                            public URIPattern load(String path) throws Exception
                            {
                                URIResolver resolver = uriResolverCache.get(path);
                                Collection<URIPattern> matches = resolver.findAll(routingTable.keySet());

                                if (matches.size() == 0)
                                {
                                    logger.warn("No matching patterns for URI " + path);
                                    throw new NotFoundException(path);
                                }
                                else
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug(matches.size() + " matching patterns for URI " + path + ". Finding best one...");
                                    }
                                    for (URIPattern p : matches)
                                    {
                                        boolean best = (p == resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH));

                                        if (best)
                                        {
                                            return p;
                                        }
                                    }

                                    return null;
                                }
                            }
                        });
    }

    private void loadRestFlowMap()
    {
        restFlowMap = new HashMap<String, Flow>();
        Collection<Flow> flows = muleContext.getRegistry().lookupObjects(Flow.class);
        for (Flow flow : flows)
        {
            String key = getRestFlowKey(flow.getName());
            if (key != null)
            {
                restFlowMap.put(key, flow);
            }
        }
        for (FlowMapping mapping : config.getFlowMappings())
        {
            restFlowMap.put(mapping.getAction() + ":" + mapping.getResource(), mapping.getFlow());
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

    private String getRestFlowKey(String name)
    {
        String[] coords = name.split(":");
        String[] methods = {"get", "put", "post", "delete", "head", "patch", "options"};
        if (coords.length < 2 || !Arrays.asList(methods).contains(coords[0]))
        {
            return null;
        }
        if (coords.length == 3 && !coords[2].equals(config.getName()))
        {
            return null;
        }
        return coords[0] + ":" + coords[1];
    }

    private void buildRoutingTable(Map<String, Resource> resources)
    {
        for (Resource resource : resources.values())
        {
            String parentUri = resource.getParentUri();
            if (parentUri.contains("{version}"))
            {
                resource.setParentUri(parentUri.replaceAll("\\{version}", getApi().getVersion()));
            }
            String uri = resource.getUri();
            logger.debug("Adding URI to the routing table: " + uri);
            routingTable.put(new URIPattern(uri), resource);
            if (resource.getResources() != null)
            {
                buildRoutingTable(resource.getResources());
            }
        }
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        HttpRestRequest request = new HttpRestRequest(event, getConfig());

        String path = request.getResourcePath();

        //check for console request
        if (config.isConsoleEnabled() && path.startsWith(getApi().getUri() + "/" + config.getConsolePath()))
        {
            return consoleHandler.process(event);
        }

        //check for raml descriptor request
        if (path.equals(getApi().getUri()) &&
            ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
            request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML))
        {
            String host = event.getMessage().getInboundProperty("host");
            if (host.contains(":"))
            {
                host = host.split(":")[0];
            }
            String raml = getRaml(host);
            event.getMessage().setPayload(raml);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, APPLICATION_RAML);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, raml.length());
            event.getMessage().setOutboundProperty("Access-Control-Allow-Origin", "*");
            return event;
        }

        URIPattern uriPattern;
        URIResolver uriResolver;
        try
        {
            uriPattern = uriPatternCache.get(path);
            uriResolver = uriResolverCache.get(path);
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof MuleRestException)
            {
                throw (MuleRestException) e.getCause();
            }
            throw new DefaultMuleException(e);
        }

        Resource resource = routingTable.get(uriPattern);
        if (resource.getAction(request.getMethod()) == null)
        {
            throw new MethodNotAllowedException(resource.getUri(), request.getMethod());
        }

        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);

        processUriParameters(resolvedVariables, resource, event);

        Flow flow = getFlow(resource, request.getMethod());
        if (flow == null)
        {
            throw new ApikitRuntimeException("Flow not found for resource: " + resource);
        }
        return request.process(flow, resource.getAction(request.getMethod()));
    }

    private void processUriParameters(ResolvedVariables resolvedVariables, Resource resource, MuleEvent event) throws InvalidUriParameterException
    {
        if (logger.isDebugEnabled())
        {
            for (String name : resolvedVariables.names())
            {
                logger.debug("        uri parameter: " + name + "=" + resolvedVariables.get(name));
            }
        }

        if (!config.isDisableValidations())
        {
            for (String key : resource.getUriParameters().keySet())
            {
                String value = (String) resolvedVariables.get(key);
                if (!resource.getUriParameters().get(key).validate(value))
                {
                    throw new InvalidUriParameterException("Invalid uri parameter value " + value + " for " + key);
                }

            }
        }
        for (String name : resolvedVariables.names())
        {
            event.getMessage().setInvocationProperty(name, resolvedVariables.get(name));
        }
    }

    private Flow getFlow(Resource resource, String method)
    {
        return restFlowMap.get(method + ":" + resource.getUri());
    }

    Map<String, Flow> getRestFlowMap()
    {
        return restFlowMap;
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

}
