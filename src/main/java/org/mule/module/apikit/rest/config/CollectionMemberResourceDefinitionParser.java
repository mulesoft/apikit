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
import org.mule.module.apikit.rest.resource.collection.CollectionMemberResource;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class CollectionMemberResourceDefinitionParser extends ChildDefinitionParser
{

    public CollectionMemberResourceDefinitionParser()
    {
        super("memberResource", CollectionMemberResource.class, false);
        addIgnored(ATTRIBUTE_NAME);
    }

    @Override
    protected void parseChild(Element element, ParserContext parserContext, BeanDefinitionBuilder builder)
    {
        String parentName = element.getParentNode()
            .getAttributes()
            .getNamedItem(ATTRIBUTE_NAME)
            .getNodeValue();
        builder.addConstructorArgValue(parentName.substring(parentName.lastIndexOf(".") + 1,
            parentName.length())
                                       + "Member");
        builder.addPropertyValue("description", element.getAttribute("doc:description"));
        super.parseChild(element, parserContext, builder);

    }

}
