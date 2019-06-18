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
import org.mule.module.apikit.config.ApikitResourcePathHandler;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.raml.interfaces.model.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    protected Flow getFlow(IResource resource, HttpRestRequest request, String version) throws UnsupportedMediaTypeException, NotFoundException
    {
        String baseKey = request.getMethod() + ":" + resource.getResolvedUri(version);
        String contentType = request.getContentTypeWithAttributes();
        Map<String, Flow> rawRestFlowMap = ((Configuration) config).getRawRestFlowMap();
        Flow flow = findFlow(rawRestFlowMap,baseKey , contentType);
        return flow;
    }

    private static Flow findFlow( Map<String, Flow> flowMap, String baseKey, String contentType) throws NotFoundException, UnsupportedMediaTypeException {
        Set<String> flows = flowMap.keySet();
        Set<ApikitResourcePathHandler> paths = flows.stream().map(ApikitResourcePathHandler::parse).collect(Collectors.toSet());
        ApikitResourcePathHandler requestPath = getRequestPath(baseKey,contentType);

        for(ApikitResourcePathHandler path : paths){
            if(path.equals(requestPath)){
                return flowMap.get(path.getCompletePath());
            }
        }

        paths = paths.stream().filter((path)-> path.getPathToResource().equals(baseKey)).collect(Collectors.toSet());
        switch (paths.size()){
            case 0 : throw new NotFoundException(baseKey);
            case 1 : return flowMap.get(paths.iterator().next().getCompletePath());
            default: throw new UnsupportedMediaTypeException();
        }
    }

    private static ApikitResourcePathHandler getRequestPath(String baseKey, String contentType) {
        if(contentType != null){
            String contentTypeWithoutCharset = contentType.replaceAll("(;\\s*charset\\s*=.[^\\;]+)","");
            return new ApikitResourcePathHandler(baseKey,contentTypeWithoutCharset);
        }

        return new ApikitResourcePathHandler(baseKey);
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