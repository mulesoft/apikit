/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.config.spring.parsers.collection.ChildListEntryDefinitionParser;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.config.spring.parsers.generic.MuleOrphanDefinitionParser;
import org.mule.config.spring.parsers.specific.ExceptionStrategyDefinitionParser;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.Console;
import org.mule.module.apikit.FlowMapping;
import org.mule.module.apikit.MappingExceptionListener;
import org.mule.module.apikit.Proxy;
import org.mule.module.apikit.ProxyConfiguration;
import org.mule.module.apikit.RestMappingExceptionStrategy;
import org.mule.module.apikit.Router;

public class ApikitNamespaceHandler extends MuleNamespaceHandler
{

    public void init()
    {
        registerBeanDefinitionParser("config", new MuleOrphanDefinitionParser(Configuration.class, true));
        registerBeanDefinitionParser("flow-mapping", new ChildDefinitionParser("flowMapping", FlowMapping.class, false));
        registerBeanDefinitionParser("router", new ChildDefinitionParser("messageProcessor", Router.class));
        registerBeanDefinitionParser("console", new ChildDefinitionParser("messageProcessor", Console.class));
        registerBeanDefinitionParser("proxy-config", new MuleOrphanDefinitionParser(ProxyConfiguration.class, true));
        registerBeanDefinitionParser("proxy", new ChildDefinitionParser("messageProcessor", Proxy.class));
        registerBeanDefinitionParser("mapping-exception-strategy", new ExceptionStrategyDefinitionParser(RestMappingExceptionStrategy.class));
        registerBeanDefinitionParser("mapping", new ChildDefinitionParser("exceptionListener", MappingExceptionListener.class, false));
        registerBeanDefinitionParser("exception", new ChildListEntryDefinitionParser("exception", "value"));

    }
}
