/*
 * $Id: FlowDefinitionParser.java 22557 2011-07-25 22:48:27Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.config;

import org.mule.api.config.MuleProperties;
import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.mule.module.wsapi.api.WebService;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class WebServiceDefinitionParser extends OrphanDefinitionParser
{
    public static final String ATTRIBUTE_INTERFACE_REF = "interface-ref";

    public WebServiceDefinitionParser(Class<? extends WebService> clazz)
    {
        super(clazz, true);
        addIgnored(ATTRIBUTE_NAME);
    }

    @java.lang.Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addConstructorArgValue(element.getAttribute(ATTRIBUTE_NAME));
        builder.addConstructorArgReference(element.getAttribute(ATTRIBUTE_INTERFACE_REF));
        builder.addConstructorArgReference(MuleProperties.OBJECT_MULE_CONTEXT);
        super.doParse(element, parserContext, builder);
    }
}
