/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.module.apikit.rest.resource.base.BaseResource;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RestResourceDefinitionParser extends ChildDefinitionParser
{

    public RestResourceDefinitionParser(Class<?> clazz)
    {
        super("resource", clazz, true);
        addIgnored(ATTRIBUTE_NAME);
        addAlias("access", "accessExpression");
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addConstructorArgValue(element.getAttribute(ATTRIBUTE_NAME));
        if (!element.getParentNode().getLocalName().equals("interface"))
        {
            builder.addConstructorArgReference(getParentBeanName(element));
        }
        else
        {
            builder.addConstructorArgValue(new BaseResource());
        }
        builder.addPropertyValue("description", element.getAttribute("doc:description"));
        super.parseChild(element, parserContext, builder);
    }

    @Override
    public String getBeanName(Element element)
    {
        return getParentBeanName(element) + "." + element.getAttribute(ATTRIBUTE_NAME);
    }
}
