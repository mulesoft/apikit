/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.StartException;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.InvalidUriParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.transport.http.HttpConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRouter implements ApiRouter
{

    private static final int URI_CACHE_SIZE = 1000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected MuleContext muleContext;
    protected FlowConstruct flowConstruct;
    protected AbstractConfiguration config;
    protected Map<URIPattern, Resource> routingTable;
    protected LoadingCache<String, URIResolver> uriResolverCache;
    protected LoadingCache<String, URIPattern> uriPatternCache;

    @Override
    public void start() throws MuleException
    {
        startConfiguration();
        config.publishConsoleUrls(muleContext.getConfiguration().getWorkingDirectory());
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
                                return new URIResolver(URICoder.encode(path, '/'));
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

    protected abstract void startConfiguration() throws StartException;

    protected Raml getApi()
    {
        return config.getApi();
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        HttpRestRequest request = getHttpRestRequest(event);

        String path = request.getResourcePath();

        MuleEvent handled = handleEvent(event, path);
        if (handled != null)
        {
            return handled;
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

    protected abstract MuleEvent handleEvent(MuleEvent event, String path) throws MuleException;

    protected abstract HttpRestRequest getHttpRestRequest(MuleEvent event);

    private String getRaml(String host)
    {
        return config.getApikitRaml(host);
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

    protected abstract Flow getFlow(Resource resource, String method);

}
