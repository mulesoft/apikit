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
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.StartException;
import org.mule.construct.Flow;
import org.mule.processor.AbstractInterceptingMessageProcessor;

import java.util.Collections;
import java.util.Set;

import org.raml.model.Resource;

public class Proxy extends AbstractInterceptingMessageProcessor implements ApiRouter
{

    private String ramlUrl;
    private boolean disableValidations;
    private ProxyRouter proxyRouter = new ProxyRouter();

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

    //schema attributes

    public String getRamlUrl()
    {
        return ramlUrl;
    }

    public void setRamlUrl(String ramlUrl)
    {
        this.ramlUrl = ramlUrl;
    }

    public boolean isDisableValidations()
    {
        return disableValidations;
    }

    public void setDisableValidations(boolean disableValidations)
    {
        this.disableValidations = disableValidations;
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

    private class ProxyRouter extends AbstractRouter
    {
        private Flow basicFlow;

        @Override
        protected void startConfiguration() throws StartException
        {
            config = new ProxyConfiguration(ramlUrl, disableValidations, next);
            config.setMuleContext(muleContext);
            try
            {
                config.initialise();
            }
            catch (InitialisationException e)
            {
                throw new RuntimeException(e);
            }
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
            MuleMessage message = event.getMessage();
            Set<String> inboundPropertyNames = message.getInboundPropertyNames();
            for (String name : inboundPropertyNames)
            {
                message.setOutboundProperty(name, message.getInboundProperty(name));
            }
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
