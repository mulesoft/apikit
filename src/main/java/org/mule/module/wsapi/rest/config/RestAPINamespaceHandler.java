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
import org.mule.webservice.rest.RestWebService;
import org.mule.webservice.rest.RestWebServiceInterface;
import org.mule.webservice.rest.action.RestCreateAction;
import org.mule.webservice.rest.action.RestRetrieveAction;
import org.mule.webservice.rest.resource.RestCollectionResource;
import org.mule.webservice.rest.resource.RestDocumentResource;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RestAPINamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("interface", new WebServiceInterfaceDefinitionParser(RestWebServiceInterface.class));
        registerBeanDefinitionParser("service", new WebServiceDefinitionParser(RestWebService.class));

        registerBeanDefinitionParser("document-resource", new ChildDefinitionParser("route", RestDocumentResource.class));
        registerBeanDefinitionParser("collection-resource", new ChildDefinitionParser("route", RestCollectionResource.class));

        registerBeanDefinitionParser("create", new ChildDefinitionParser("action", RestCreateAction.class));
        registerBeanDefinitionParser("retrieve", new ChildDefinitionParser("action", RestRetrieveAction.class));
    }

}
