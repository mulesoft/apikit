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

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class WebServiceOperationDefinitionParser extends ChildDefinitionParser
{

    public static final String ATTRIBUTE_FLOW_REF = "flow-ref";

    public WebServiceOperationDefinitionParser(String setterMethod, Class<?> clazz)
    {
        super(setterMethod, clazz);
        addIgnored(ATTRIBUTE_NAME);
        addIgnored(ATTRIBUTE_FLOW_REF);
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addConstructorArgValue(element.getAttribute(ATTRIBUTE_NAME));
        builder.addConstructorArgReference(element.getAttribute(ATTRIBUTE_FLOW_REF));
        super.parseChild(element, parserContext, builder);
    }
}
