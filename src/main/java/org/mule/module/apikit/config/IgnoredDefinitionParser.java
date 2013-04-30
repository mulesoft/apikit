/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class IgnoredDefinitionParser implements BeanDefinitionParser
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
