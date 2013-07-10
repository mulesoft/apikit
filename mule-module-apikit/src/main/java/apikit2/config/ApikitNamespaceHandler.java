package apikit2.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.config.spring.parsers.collection.ChildListEntryDefinitionParser;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.config.spring.parsers.generic.MuleOrphanDefinitionParser;
import org.mule.config.spring.parsers.specific.ExceptionStrategyDefinitionParser;

import apikit2.Configuration;
import apikit2.MappingExceptionListener;
import apikit2.RestMappingExceptionStrategy;
import apikit2.Router;

public class ApikitNamespaceHandler extends MuleNamespaceHandler
{

    public void init()
    {
        registerBeanDefinitionParser("config", new MuleOrphanDefinitionParser(Configuration.class, true));
        registerBeanDefinitionParser("router", new ChildDefinitionParser("messageProcessor", Router.class));
        registerBeanDefinitionParser("mapping-exception-strategy", new ExceptionStrategyDefinitionParser(RestMappingExceptionStrategy.class));
        registerBeanDefinitionParser("mapping", new ChildDefinitionParser("exceptionListener", MappingExceptionListener.class, false));
        registerBeanDefinitionParser("exception", new ChildListEntryDefinitionParser("exception", "value"));

    }
}
