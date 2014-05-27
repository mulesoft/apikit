/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.StartException;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;
import org.mule.processor.AbstractInterceptingMessageProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.raml.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy extends AbstractInterceptingMessageProcessor implements ApiRouter
{

    public static final Set<String> MULE_REQUEST_HEADERS;
    public static final Set<String> MULE_RESPONSE_HEADERS;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private ProxyRouter proxyRouter = new ProxyRouter();

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
        MULE_RESPONSE_HEADERS = new HashSet<String>(Arrays.asList(headers));
        MULE_REQUEST_HEADERS = new HashSet<String>(MULE_RESPONSE_HEADERS);
        MULE_REQUEST_HEADERS.remove("http.method");
    }

    @Override
    public void start() throws MuleException
    {
        proxyRouter.start();
    }

    @Override
    public MuleEvent process(MuleEvent muleEvent) throws MuleException
    {
        return proxyRouter.process(muleEvent);
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        proxyRouter.setFlowConstruct(flowConstruct);
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        super.setMuleContext(context);
        proxyRouter.setMuleContext(context);
    }

    public ProxyConfiguration getConfig()
    {
        return proxyRouter.getConfig();
    }

    public void setConfig(ProxyConfiguration config)
    {
        proxyRouter.setConfig(config);
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

    private class ProxyRouter extends AbstractRouter
    {

        private Flow basicFlow;

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
            return null;
        }

        @Override
        protected HttpRestRequest getHttpRestRequest(MuleEvent event)
        {
            return new HttpRestProxyRequest(event, config);
        }

        @Override
        protected Flow getFlow(Resource resource, String method)
        {
            FlowResolver flowResolver = config.getRestFlowMap().get(method + ":" + resource.getUri());
            Flow rawFlow = ((ProxyConfiguration.ProxyFlowResolver) flowResolver).getRawFlow();
            if (rawFlow == null)
            {
                rawFlow = basicFlow;
            }
            return rawFlow;
        }
    }
}
