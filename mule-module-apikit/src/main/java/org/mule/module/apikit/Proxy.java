/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.StartException;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy extends AbstractRouter
{

    public static final Set<String> MULE_REQUEST_HEADERS;
    public static final Set<String> MULE_RESPONSE_HEADERS;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private Flow basicFlow;

    static
    {
        String[] headers =
                {
                        "http.context.path",
                        "http.context.uri",
                        "http.headers",
                        "http.method",
                        "http.query.params",
                        "http.query.string",
                        "http.relative.path",
                        "http.request",
                        "http.request.path",
                        "http.status",
                        "http.version",
                        "server",
                        "x-mule_encoding",
                        "x-mule_session",
                        "MULE_ORIGINATING_ENDPOINT",
                        "MULE_REMOTE_CLIENT_ADDRESS"
                };
        MULE_RESPONSE_HEADERS = new HashSet<>(Arrays.asList(headers));
        MULE_RESPONSE_HEADERS.remove("http.status");
        MULE_REQUEST_HEADERS = new HashSet<>(MULE_RESPONSE_HEADERS);
        MULE_REQUEST_HEADERS.remove("http.method");
    }

    public ProxyConfiguration getConfig()
    {
        return (ProxyConfiguration) config;
    }

    public void setConfig(ProxyConfiguration config)
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
                config = muleContext.getRegistry().lookupObject(ProxyConfiguration.class);
            }
            catch (RegistrationException e)
            {
                throw new StartException(MessageFactory.createStaticMessage("APIKit Proxy configuration not Found"), this);
            }
        }
        ((ProxyConfiguration) config).setChain(next);
        config.initializeRestFlowMapWrapper();
        config.loadApiDefinition(flowConstruct);
        basicFlow = buildBasicFlow();
    }

    private Flow buildBasicFlow()
    {
        String flowName = "__intercepted_chain_flow";
        Flow wrapper = new Flow(flowName, muleContext);
        wrapper.setMessageProcessors(Collections.singletonList(next));
        try
        {
            muleContext.getRegistry().registerFlowConstruct(wrapper);
        }
        catch (MuleException e)
        {
            throw new RuntimeException("Error registering flow " + flowName, e);
        }
        return wrapper;
    }

    @Override
    protected MuleEvent handleEvent(MuleEvent event, String path) throws MuleException
    {
        copyProperties(event, MULE_REQUEST_HEADERS);
        event.getMessage().setOutboundProperty("http.disable.status.code.exception.check", "true");
        return null;
    }

    @Override
    protected Flow getFlow(Resource resource, HttpRestRequest request)
    {
        FlowResolver flowResolver = config.getRestFlowMap().get(request.getMethod() + ":" + resource.getUri());
        Flow rawFlow = ((ProxyConfiguration.ProxyFlowResolver) flowResolver).getRawFlow();
        if (rawFlow == null)
        {
            rawFlow = basicFlow;
        }
        return rawFlow;
    }

    @Override
    protected MuleEvent doProcessRouterResponse(MuleEvent event, Integer successStatus)
    {
        Proxy.copyProperties(event, Proxy.MULE_RESPONSE_HEADERS);
        return event;
    }

    public static void copyProperties(MuleEvent event, Set<String> skip)
    {
        MuleMessage message = event.getMessage();
        Set<String> inboundPropertyNames = message.getInboundPropertyNames();
        for (String name : inboundPropertyNames)
        {
            if (!skip.contains(name))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(String.format(">>>>>>>  copying header %s -> %s", name, message.getInboundProperty(name)));
                }
                message.setOutboundProperty(name, message.getInboundProperty(name));
            }
            else
            {
                LOGGER.debug(String.format("/////// skipping header %s -> %s", name, message.getInboundProperty(name)));
            }
        }
    }

}
