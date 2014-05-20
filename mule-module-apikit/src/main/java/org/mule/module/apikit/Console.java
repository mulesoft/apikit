/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.Configuration.APPLICATION_RAML;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.http.HttpConstants;

import org.raml.model.ActionType;
import org.raml.model.Raml;

public class Console implements MessageProcessor, Initialisable, MuleContextAware, FlowConstructAware
{

    private AbstractConfiguration config;
    private MuleContext muleContext;
    private ConsoleHandler consoleHandler;
    private FlowConstruct flowConstruct;

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

    public void setConfig(AbstractConfiguration config)
    {
        this.config = config;
    }

    public AbstractConfiguration getConfig()
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
    public void initialise() throws InitialisationException
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
                throw new InitialisationException(MessageFactory.createStaticMessage("APIKit configuration not Found"), this);
            }
        }
        consoleHandler = new ConsoleHandler(getConfig().getEndpointAddress(flowConstruct), "");
        config.addConsoleUrl(consoleHandler.getConsoleUrl());
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        HttpRestRequest request = new HttpRestRouterRequest(event, getConfig());

        String path = request.getResourcePath();

        //check for raml descriptor request
        if ((path.isEmpty() || path.equals("/")) &&
            ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
            request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML))
        {
            String raml = getRaml((String) event.getMessage().getInboundProperty("http.host"));
            event.getMessage().setPayload(raml);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, APPLICATION_RAML);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, raml.length());
            event.getMessage().setOutboundProperty("Access-Control-Allow-Origin", "*");
            return event;
        }

        return consoleHandler.process(event);
    }
}
