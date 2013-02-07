/*
 * $Id: RssNamespaceHandler.java 21236 2011-02-10 05:12:40Z dirk.olmes $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.config;

import org.mule.config.spring.parsers.generic.TextDefinitionParser;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class APINamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("mule-api", new IgnoredDefinitionParser());
        registerBeanDefinitionParser("description", new TextDefinitionParser("description"));

    }

    private static class IgnoredDefinitionParser implements BeanDefinitionParser
    {
        public IgnoredDefinitionParser()
        {
            super();
        }

        @Override
        public BeanDefinition parse(Element element, ParserContext parserContext)
        {
            return null;
        }
    }
}
