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
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.ResourceLoader;

public class ProxyConfiguration extends AbstractConfiguration
{

    private MessageProcessor chain;

    public void setChain(MessageProcessor chain)
    {
        this.chain = chain;
    }

    @Override
    protected void initializeRestFlowMap()
    {
    }

    @Override
    protected HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return new HttpRestProxyRequest(event, this);
    }

    @Override
    public ResourceLoader getRamlResourceLoader()
    {
        return new CompositeResourceLoader(new RamlUrlResourceLoader(), new ClassPathResourceLoader());
    }

    private static class RamlUrlResourceLoader implements ResourceLoader
    {

        @Override
        public InputStream fetchResource(String resourceName)
        {
            InputStream inputStream = null;
            try
            {
                URL url = new URL(resourceName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", APPLICATION_RAML + ", */*");
                inputStream = new BufferedInputStream(connection.getInputStream());
            }
            catch (IOException e)
            {
                //ignore on resource not found
            }
            return inputStream;

        }
    }

    @Override
    protected void initializeRestFlowMapWrapper()
    {
        if (chain != null)
        {
            super.initializeRestFlowMapWrapper();
        }
    }

    @Override
    protected FlowResolver getFlowResolver(AbstractConfiguration configuration, String key)
    {
        return new ProxyFlowResolver((ProxyConfiguration) configuration, key, chain);
    }

    static class ProxyFlowResolver implements FlowResolver
    {

        private static final String WRAPPER_FLOW_SUFFIX = "-gateway-wrapper";

        private ProxyConfiguration configuration;
        private String key;
        private MessageProcessor chain;
        private Flow wrapperFlow;

        ProxyFlowResolver(ProxyConfiguration configuration, String key, MessageProcessor chain)
        {
            this.configuration = configuration;
            this.key = key;
            this.chain = chain;
        }

        @Override
        public Flow getFlow()
        {
            //already wrapped
            if (wrapperFlow != null)
            {
                return wrapperFlow;
            }

            //wrap target
            wrapperFlow = wrapFlow();
            return wrapperFlow;
        }

        protected Flow getRawFlow()
        {
            return wrapperFlow;
        }

        private Flow wrapFlow()
        {
            String flowName = key + WRAPPER_FLOW_SUFFIX;
            MuleContext muleContext = configuration.getMuleContext();
            Flow wrapper = new Flow(flowName, muleContext);
            wrapper.setMessageProcessors(Collections.singletonList(chain));
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
    }

}
