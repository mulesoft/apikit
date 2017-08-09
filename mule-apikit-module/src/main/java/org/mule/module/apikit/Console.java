/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.console.ConsoleResources;
import org.mule.module.apikit.api.console.Resource;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.helpers.EventWrapper;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.meta.AbstractAnnotatedObject;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.StringMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;

public class Console extends AbstractAnnotatedObject implements Processor, Initialisable
{
    private final ApikitRegistry registry;
    private final ConfigurationComponentLocator locator;
    private Configuration config;

    private String configRef;
    private String name;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public Console(ApikitRegistry registry, ConfigurationComponentLocator locator) {
        this.registry = registry;
        this.locator = locator;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        final String name = getLocation().getRootContainerName();

        final Optional<URI> url = locator.find(Location.builder().globalName(name).addSourcePart().build())
                .map(MessageSourceUtils::getUriFromFlow);

        if (!url.isPresent()) {
            logger.error("There was an error retrieving console source.");
            return;
        }

        url.ifPresent(uri -> logger.info(StringMessageUtils.getBoilerPlate("APIKit ConsoleURL : " + uri.toString().replace("*", ""))));
    }

    @Override
    public Event process(Event event) throws MuleException
    {
        config = registry.getConfiguration(getConfigRef());

        EventWrapper eventWrapper = new EventWrapper(event, config.getOutboundHeadersMapName(), config.getHttpStatusVarName());

        HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(event);
        String listenerPath = attributes.getListenerPath();
        String requestPath= attributes.getRequestPath();
        String acceptHeader = AttributesHelper.getHeaderIgnoreCase(attributes,"Accept");
        String queryString = attributes.getQueryString();
        String method = attributes.getMethod();

        ConsoleResources consoleResources = new ConsoleResources(config, listenerPath, requestPath, queryString, method, acceptHeader);

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
