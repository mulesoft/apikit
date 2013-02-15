/*
 * $Id: RssNamespaceHandler.java 21236 2011-02-10 05:12:40Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.ws.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.module.wsapi.config.WebServiceDefinitionParser;
import org.mule.module.wsapi.config.WebServiceInterfaceDefinitionParser;
import org.mule.module.wsapi.ws.WSDLOperation;
import org.mule.module.wsapi.ws.WSWebService;
import org.mule.module.wsapi.ws.WSWebServiceInterface;

public class WSAPINamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
        registerBeanDefinitionParser("interface", new WebServiceInterfaceDefinitionParser(
            WSWebServiceInterface.class));
        registerBeanDefinitionParser("service", new WebServiceDefinitionParser(WSWebService.class));
        registerBeanDefinitionParser("operation", new WebServiceOperationDefinitionParser("route", WSDLOperation.class));
    }

}
