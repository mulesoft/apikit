/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.parsers.generic.AutoIdUtils;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RestOperationDefinitionParser extends ChildDefinitionParser
{
    public RestOperationDefinitionParser(Class<?> clazz)
    {
        super("operation", clazz, false);
        addAlias("access", "accessExpression");
        addAlias("flow", "handler");
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addPropertyValue("description", element.getAttribute("doc:description"));
        super.parseChild(element, parserContext, builder);
    }

    @Override
    public String getBeanName(Element element)
    {
        return AutoIdUtils.uniqueValue(element.getParentNode()
            .getAttributes()
            .getNamedItem(ATTRIBUTE_NAME)
            .getNodeValue() + "." + element.getAttribute(ATTRIBUTE_NAME));
    }

}
