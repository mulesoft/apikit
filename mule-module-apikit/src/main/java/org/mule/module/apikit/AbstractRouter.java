/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

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
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;

import com.google.common.cache.LoadingCache;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRouter implements ApiRouter
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected MuleContext muleContext;
    protected FlowConstruct flowConstruct;
    protected AbstractConfiguration config;
    protected RamlDescriptorHandler ramlHandler;

    @Override
    public void start() throws MuleException
    {
        startConfiguration();
        ramlHandler = new RamlDescriptorHandler(config);
        config.publishConsoleUrls(muleContext.getConfiguration().getWorkingDirectory());
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
        if (ramlHandler.handles(request))
        {
            return ramlHandler.processRouterRequest(event);
        }

        URIPattern uriPattern;
        URIResolver uriResolver;
        path = path.isEmpty() ? "/" : path;
        try
        {
            uriPattern = getUriPatternCache().get(path);
            uriResolver = getUriResolverCache().get(path);
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof MuleRestException)
            {
                throw (MuleRestException) e.getCause();
            }
            throw new DefaultMuleException(e);
        }

        Resource resource = getRoutingTable().get(uriPattern);
        if (resource.getAction(request.getMethod()) == null)
        {
            throw new MethodNotAllowedException(resource.getUri(), request.getMethod());
        }

        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);

        processUriParameters(resolvedVariables, resource, event);

        Flow flow = getFlow(resource, request);
        if (flow == null)
        {
            throw new ApikitRuntimeException("Flow not found for resource: " + resource);
        }
        return request.process(flow, resource.getAction(request.getMethod()));
    }

    private Map<URIPattern, Resource> getRoutingTable()
    {
        return config.routingTable;
    }

    private LoadingCache<String, URIResolver> getUriResolverCache()
    {
        return config.uriResolverCache;
    }

    private LoadingCache<String, URIPattern> getUriPatternCache()
    {
        return config.uriPatternCache;
    }

    protected abstract MuleEvent handleEvent(MuleEvent event, String path) throws MuleException;

    private HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return config.getHttpRestRequest(event);
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
            for (Map.Entry<String, UriParameter> entry : resource.getResolvedUriParameters().entrySet())
            {
                String value = (String) resolvedVariables.get(entry.getKey());
                UriParameter uriParameter = entry.getValue();
                if (!uriParameter.validate(value))
                {
                    String msg = String.format("Invalid value '%s' for uri parameter %s. %s",
                                               value, entry.getKey(), uriParameter.message(value));
                    throw new InvalidUriParameterException(msg);
                }

            }
        }
        for (String name : resolvedVariables.names())
        {
            event.getMessage().setInvocationProperty(name, resolvedVariables.get(name));
        }
    }

    protected abstract Flow getFlow(Resource resource, HttpRestRequest request);

}
