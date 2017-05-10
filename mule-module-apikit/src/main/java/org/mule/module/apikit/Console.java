/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.processor.Processor;

import javax.inject.Inject;

import java.util.HashMap;

public class Console implements Processor
{
    @Inject
    private ApikitRegistry registry;

    private String configRef;
    private String name;
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public Event process(Event event) throws MuleException
    {
        Configuration config = registry.getConfiguration(getConfigRef());
        event = EventHelper.addVariable(event, config.getOutboundHeadersMapName(), new HashMap<>());
        event = EventHelper.addVariable(event, config.getHttpStatusVarName(), "200");

        HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

        String relativePath = UrlUtils.getRelativePath(attributes);

        if (config.getRamlHandler().isRequestingRamlV1(attributes))
        {
            String raml =config.getRamlHandler().getRamlV1();
            return EventHelper.setPayload(event, raml, RamlHandler.APPLICATION_RAML);
        }
        if (config.getRamlHandler().isRequestingRamlV2(attributes))
        {
            String raml = config.getRamlHandler().getRamlV2(relativePath);
            return EventHelper.setPayload(event, raml, RamlHandler.APPLICATION_RAML);
        }

        return event;
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