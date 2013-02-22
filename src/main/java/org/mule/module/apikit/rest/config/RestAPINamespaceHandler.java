/*
 * $Id: RssNamespaceHandler.java 21236 2011-02-10 05:12:40Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.module.apikit.config.WebServiceInterfaceDefinitionParser;
import org.mule.module.apikit.rest.RestWebServiceInterface;
import org.mule.module.apikit.rest.resource.collection.CollectionResource;
import org.mule.module.apikit.rest.resource.document.DocumentResource;

public class RestAPINamespaceHandler extends MuleNamespaceHandler
{
    public void init()
    {
        registerBeanDefinitionParser("interface", new WebServiceInterfaceDefinitionParser(
            RestWebServiceInterface.class));
        registerBeanDefinitionParser("service", new RestWebServiceDefinitionParser());
        registerBeanDefinitionParser("document-resource", new RestResourceDefinitionParser(
            DocumentResource.class));
        registerBeanDefinitionParser("collection-resource", new RestResourceDefinitionParser(
            CollectionResource.class));

        registerBeanDefinitionParser("create", new RestCreateOperationDefinitionParser());
        registerBeanDefinitionParser("update", new RestUpdateOperationDefinitionParser());
        registerBeanDefinitionParser("retrieve", new RestRetrieveOperationDefinitionParser());
        registerBeanDefinitionParser("exists", new RestExistsOperationDefinitionParser());

        registerBeanDefinitionParser("representation", new RepresentationDefinitionParser("representation"));
    }

}
