/*
 * $Id: RssNamespaceHandler.java 21236 2011-02-10 05:12:40Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest.config;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.module.wsapi.config.WebServiceDefinitionParser;
import org.mule.module.wsapi.config.WebServiceInterfaceDefinitionParser;
import org.mule.webservice.rest.ReSTCreateAction;
import org.mule.webservice.rest.ReSTDcoumentResource;
import org.mule.webservice.rest.ReSTWebService;
import org.mule.webservice.rest.ReSTWebServiceInterface;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ReSTAPINamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("interface", new WebServiceInterfaceDefinitionParser(ReSTWebServiceInterface.class));
        registerBeanDefinitionParser("service", new WebServiceDefinitionParser(ReSTWebService.class));
        registerBeanDefinitionParser("document-resource", new ChildDefinitionParser("route", ReSTDcoumentResource.class));
        registerBeanDefinitionParser("create", new ChildDefinitionParser("action", ReSTCreateAction.class));
    }

}
