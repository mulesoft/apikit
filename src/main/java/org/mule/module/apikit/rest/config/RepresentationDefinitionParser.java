/*
 * $Id: FlowDefinitionParser.java 22557 2011-07-25 22:48:27Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.module.apikit.rest.representation.DefaultRepresentation;

import com.google.common.net.MediaType;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RepresentationDefinitionParser extends ChildDefinitionParser
{

    public RepresentationDefinitionParser(String setter)
    {
        super(setter, DefaultRepresentation.class, false);
        addIgnored(ATTRIBUTE_NAME);
        addIgnored("mediaType");
        addIgnored("quality");
        addAlias("access", "accessExpression");
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        builder.addPropertyValue("mediaType",
            MediaType.parse(element.getAttribute("mediaType") + ";q=" + element.getAttribute("quality")));
        super.parseChild(element, parserContext, builder);
    }
}
