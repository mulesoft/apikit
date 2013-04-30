/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.ws.config;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class WebServiceOperationDefinitionParser extends ChildDefinitionParser
{

    public WebServiceOperationDefinitionParser(String setterMethod, Class<?> clazz)
    {
        super(setterMethod, clazz);
        addIgnored(ATTRIBUTE_NAME);
        addAlias("flow", "handler");
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addConstructorArgValue(element.getAttribute(ATTRIBUTE_NAME));
        builder.addPropertyValue("description", element.getAttribute("doc:description"));
        super.parseChild(element, parserContext, builder);
    }
}
