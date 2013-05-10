/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.module.apikit.rest.representation.DefaultRepresentationMetaData;
import org.mule.util.StringUtils;

import com.google.common.net.MediaType;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RepresentationDefinitionParser extends ChildDefinitionParser
{

    public RepresentationDefinitionParser(String setter)
    {
        super(setter, DefaultRepresentationMetaData.class, false);
        addIgnored(ATTRIBUTE_NAME);
        addIgnored("mediaType");
        addIgnored("quality");
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        String quality = defaultIfEmpty(element.getAttribute("quality"), "1");
        builder.addPropertyValue("mediaType", MediaType.parse(element.getAttribute("mediaType") + ";q=" + quality));
        super.parseChild(element, parserContext, builder);
    }
}
