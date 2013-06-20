package apikit2.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.config.spring.parsers.collection.ChildListEntryDefinitionParser;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.config.spring.parsers.specific.ExceptionStrategyDefinitionParser;

import apikit2.MappingExceptionListener;
import apikit2.RestMappingExceptionStrategy;
import apikit2.RestProcessor;

public class ApikitNamespaceHandler extends MuleNamespaceHandler
{

    public void init()
    {
        registerBeanDefinitionParser("rest-processor", new ChildDefinitionParser("messageProcessor", RestProcessor.class));
        registerBeanDefinitionParser("flow", new RestFlowDefinitionParser());
        registerBeanDefinitionParser("mapping-exception-strategy", new ExceptionStrategyDefinitionParser(RestMappingExceptionStrategy.class));
        registerBeanDefinitionParser("mapping", new ChildDefinitionParser("exceptionListener", MappingExceptionListener.class, false));
        registerBeanDefinitionParser("exception", new ChildListEntryDefinitionParser("exception", "value"));

    }
}
