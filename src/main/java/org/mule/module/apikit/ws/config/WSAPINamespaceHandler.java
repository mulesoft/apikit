/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.ws.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.module.apikit.config.WebServiceDefinitionParser;
import org.mule.module.apikit.config.WebServiceInterfaceDefinitionParser;
import org.mule.module.apikit.ws.WSDLOperation;
import org.mule.module.apikit.ws.WSWebService;
import org.mule.module.apikit.ws.WSWebServiceInterface;

public class WSAPINamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
        registerBeanDefinitionParser("api", new org.mule.module.apikit.config.IgnoredDefinitionParser());
        registerBeanDefinitionParser("interface", new WebServiceInterfaceDefinitionParser(
            WSWebServiceInterface.class));
        registerBeanDefinitionParser("service", new WebServiceDefinitionParser(WSWebService.class));
        registerBeanDefinitionParser("operation", new WebServiceOperationDefinitionParser("route",
            WSDLOperation.class));
    }

}
