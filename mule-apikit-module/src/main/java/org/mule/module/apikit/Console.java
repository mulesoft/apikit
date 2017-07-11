/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import javax.inject.Inject;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.console.ConsoleResources;
import org.mule.module.apikit.api.console.Resource;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.helpers.EventWrapper;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.api.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console implements Processor, FlowConstructAware
{
    @Inject
    private ApikitRegistry registry;
    private Configuration config;

    private String configRef;
    private String name;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    FlowConstruct flowConstruct;

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    @Override
    public Event process(Event event) throws MuleException
    {
        config = registry.getConfiguration(getConfigRef());

        EventWrapper eventWrapper = new EventWrapper(event, config.getOutboundHeadersMapName(), config.getHttpStatusVarName());

        HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(event);
        String listenerPath = attributes.getListenerPath();
        String requestPath= attributes.getRequestPath();
        String aceptHeader = AttributesHelper.getHeaderIgnoreCase(attributes,"Accept");
        String queryString = attributes.getQueryString();
        String method = attributes.getMethod();

        ConsoleResources consoleResources = new ConsoleResources(config, listenerPath, requestPath, queryString, method, aceptHeader);

        // Listener path MUST end with /*
        consoleResources.isValidPath(attributes.getListenerPath());

        String consoleBasePath = UrlUtils.getBasePath(listenerPath, requestPath);
        String resourceRelativePath = UrlUtils.getRelativePath(listenerPath, requestPath);

        // If the request was made to, for example, /console, we must redirect the client to /console/
        if (!consoleBasePath.endsWith("/"))
        {
            eventWrapper.doClientRedirect();
            return eventWrapper.build();
        }

        Resource resource = consoleResources.getConsoleResource(resourceRelativePath);

        eventWrapper.setPayload(resource.getContent(), resource.getMediaType());
        eventWrapper.addOutboundProperties(resource.getHeaders());

        return eventWrapper.build();
    }

    public String getConfigRef()
    {
        return configRef;
    }

    public void setConfigRef(String config)
    {
        this.configRef = config;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
