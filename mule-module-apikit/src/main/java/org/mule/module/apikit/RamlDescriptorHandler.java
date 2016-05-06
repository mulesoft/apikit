/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.http.HttpConstants;

import org.raml.model.ActionType;

public class RamlDescriptorHandler
{

    private AbstractConfiguration config;

    public RamlDescriptorHandler(AbstractConfiguration config)
    {
        this.config = config;
    }

    public boolean handles(HttpRestRequest request)
    {
        String path = request.getResourcePath();
        return (!config.isParserV2() && isValidPath(path) &&
                ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
                request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML));
    }

    private boolean isValidPath(String path)
    {
        if (config instanceof Configuration && ((Configuration) config).isConsoleEnabled())
        {
            if (path.equals("/" + ((Configuration) config).getConsolePath()))
            {
                return true;
            }
        }
        return path.equals(config.getApi().getUri());
    }

    public MuleEvent processConsoleRequest(MuleEvent event) throws MuleException
    {
        return process(event, config.getApikitRamlConsole(event));
    }

    public MuleEvent processRouterRequest(MuleEvent event) throws MuleException
    {
        return process(event, config.getApikitRaml(event));
    }

    private MuleEvent process(MuleEvent event, String raml) throws MuleException
    {
        event.getMessage().setPayload(raml, DataTypeFactory.create(String.class, APPLICATION_RAML));
        event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, APPLICATION_RAML);
        event.getMessage().setOutboundProperty(HttpConstants.HEADER_EXPIRES, -1); //avoid IE ajax response caching
        event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, raml.length());
        event.getMessage().setOutboundProperty("Access-Control-Allow-Origin", "*");
        return event;
    }
}
