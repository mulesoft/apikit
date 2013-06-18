package apikit2.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;

import apikit2.RestProcessor;

public class ApikitNamespaceHandler extends MuleNamespaceHandler
{

    public void init()
    {
        registerMuleBeanDefinitionParser("rest-processor", new ChildDefinitionParser("messageProcessor", RestProcessor.class));
        registerBeanDefinitionParser("flow", new RestFlowDefinitionParser());
    }
}
