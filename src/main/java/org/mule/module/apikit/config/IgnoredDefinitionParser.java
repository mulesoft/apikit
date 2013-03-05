
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
