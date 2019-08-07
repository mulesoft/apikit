/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.StartException;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.raml.interfaces.model.IResource;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router extends AbstractRouter
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ConsoleHandler consoleHandler;

    public Configuration getConfig()
    {
        return (Configuration) config;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    @Override
    protected void startConfiguration() throws StartException
    {
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
        config.loadApiDefinition(flowConstruct);
        if (getConfig().isConsoleEnabled())
        {
            consoleHandler = new ConsoleHandler(getConfig().getEndpointAddress(flowConstruct), getConfig().getConsolePath(), config);
            consoleHandler.updateRamlUri();
            getConfig().addConsoleUrl(consoleHandler.getConsoleUrl());
        }
    }

    @Override
    protected MuleEvent handleEvent(MuleEvent event, String path) throws MuleException
    {
        //check for console request
        if (getConfig().isConsoleEnabled() && path.startsWith("/" + getConfig().getConsolePath()))
        {
            return consoleHandler.process(event);
        }
        return null;
    }

    /**
     * Returns the flow that handles the request or null if there is none.
     * First tries to match a flow by method, resource and content type,
     * if there is no match it retries using method and resource only.
     */
    @Override
    protected Flow getFlow(IResource resource, HttpRestRequest request, String version) throws UnsupportedMediaTypeException
    {
        String baseKey = request.getMethod() + ":" + resource.getResolvedUri(version);
        String contentType = request.getContentType() != null ? request.getContentType().toLowerCase() : "";
        Map<String, Flow> rawRestFlowMap = ((Configuration) config).getRawRestFlowMap();
        Flow flow = rawRestFlowMap.get(baseKey + ":" + contentType);
        if (flow == null)
        {
            flow = rawRestFlowMap.get(baseKey);
            if (flow == null && isFlowDeclaredWithDifferentMediaType(rawRestFlowMap, baseKey))
            {
                throw new UnsupportedMediaTypeException();
            }
        }
        return flow;
    }

    protected boolean isFlowDeclaredWithDifferentMediaType(Map<String, Flow> map, String baseKey)
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

    @Override
    protected MuleEvent doProcessRouterResponse(MuleEvent event, Integer successStatus)
    {
        if (event.getMessage().getOutboundProperty("http.status") == null)
        {
            event.getMessage().setOutboundProperty("http.status", successStatus);
        }
        return event;
    }

}
