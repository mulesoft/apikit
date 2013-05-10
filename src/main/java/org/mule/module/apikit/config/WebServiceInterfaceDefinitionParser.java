/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.config;

import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class WebServiceInterfaceDefinitionParser extends OrphanDefinitionParser
{
    public WebServiceInterfaceDefinitionParser(Class<?> clazz)
    {
        super(clazz, true);
        addIgnored(ATTRIBUTE_NAME);
    }

    @java.lang.Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addConstructorArgValue(element.getAttribute(ATTRIBUTE_NAME));
        super.doParse(element, parserContext, builder);
    }
}
