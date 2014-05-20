/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.config.MuleProperties;
import org.mule.api.processor.DynamicPipelineException;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.transform.ApikitResponseTransformer;
import org.mule.util.IOUtils;
import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.NodeRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration extends AbstractConfiguration
{

    public static final String DEFAULT_CONSOLE_PATH = "console";
    private static final String CONSOLE_URL_FILE = "consoleurl";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;
    private boolean consoleEnabled = true;
    private String consolePath = DEFAULT_CONSOLE_PATH;
    private List<FlowMapping> flowMappings = new ArrayList<FlowMapping>();
    private List<String> consoleUrls = new ArrayList<String>();
    private Map<String, Flow> restFlowMap;
    private boolean started;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRaml()
    {
        return raml;
    }

    public void setRaml(String raml)
    {
        this.raml = raml;
    }

    public boolean isConsoleEnabled()
    {
        return consoleEnabled;
    }

    public void setConsoleEnabled(boolean consoleEnabled)
    {
        this.consoleEnabled = consoleEnabled;
    }

    public String getConsolePath()
    {
        return consolePath;
    }

    public void setConsolePath(String consolePath)
    {
        this.consolePath = consolePath;
    }

    public List<FlowMapping> getFlowMappings()
    {
        return flowMappings;
    }

    public void setFlowMappings(List<FlowMapping> flowMappings)
    {
        this.flowMappings = flowMappings;
    }

    @Override
    public ResourceLoader getRamlResourceLoader()
    {
        ResourceLoader loader = new DefaultResourceLoader();
        String appHome = muleContext.getRegistry().get(MuleProperties.APP_HOME_DIRECTORY_PROPERTY);
        if (appHome != null)
        {
            loader = new CompositeResourceLoader(new FileResourceLoader(appHome), loader);
        }
        return loader;
    }

    @Override
    protected NodeRuleFactory getValidatorNodeRuleFactory()
    {
        return new NodeRuleFactory(new ActionImplementedRuleExtension(restFlowMap));
    }

    public void addConsoleUrl(String url)
    {
        if (StringUtils.isNotBlank(url))
        {
            consoleUrls.add(url);
        }
    }

    public void publishConsoleUrls(String parentDirectory)
    {
        started = true;
        if (isLastRouterToStart())
        {
            dumpUrlsFile(parentDirectory);
        }

        if (logger.isInfoEnabled())
        {
            for (String consoleUrl : consoleUrls)
            {
                String msg = String.format("APIKit Console URL: %s", consoleUrl);
                logger.info(StringMessageUtils.getBoilerPlate(msg));
            }
        }
    }

    private boolean isLastRouterToStart()
    {
        Collection<Configuration> configurations = muleContext.getRegistry().lookupObjects(Configuration.class);
        for (Configuration configuration : configurations)
        {
            if (!configuration.started)
            {
                return false;
            }
        }
        return true;
    }

    private void dumpUrlsFile(String parentDirectory)
    {
        File urlFile = new File(parentDirectory, CONSOLE_URL_FILE);
        FileWriter writer = null;
        try
        {
            if (!urlFile.exists())
            {
                urlFile.createNewFile();
            }
            writer = new FileWriter(urlFile, true);


            for (String consoleUrl : getAllConsoleUrls())
            {
                writer.write(consoleUrl + "\n");
            }

            writer.flush();
        }
        catch (IOException e)
        {
            logger.error("cannot publish console url for studio", e);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private List<String> getAllConsoleUrls()
    {
        List<String> urls = new ArrayList<String>();
        Collection<Configuration> configurations = muleContext.getRegistry().lookupObjects(Configuration.class);
        for (Configuration configuration : configurations)
        {
            urls.addAll(configuration.consoleUrls);
        }
        return urls;
    }

    protected void initializeRestFlowMap()
    {
        if (restFlowMap == null)
        {
            restFlowMap = new HashMap<String, Flow>();

            //init flows by convention
            Collection<Flow> flows = muleContext.getRegistry().lookupObjects(Flow.class);
            for (Flow flow : flows)
            {
                String key = getRestFlowKey(flow.getName());
                if (key != null)
                {
                    restFlowMap.put(key, flow);
                }
            }

            //init flow mappings
            for (FlowMapping mapping : getFlowMappings())
            {
                restFlowMap.put(mapping.getAction() + ":" + mapping.getResource(), mapping.getFlow());
            }

            addResponseTransformers(restFlowMap.values());

            if (logger.isDebugEnabled())
            {
                logger.debug("==== RestFlows defined:");
                for (String key : restFlowMap.keySet())
                {
                    logger.debug("\t\t" + key);
                }
            }
        }
    }

    private void addResponseTransformers(Collection<Flow> flows)
    {
        for (Flow flow : flows)
        {
            try
            {
                flow.dynamicPipeline(null).injectAfter(new ApikitResponseTransformer()).resetAndUpdate();
            }
            catch (DynamicPipelineException e)
            {
                //ignore, transformer already added
            }
            catch (MuleException e)
            {
                throw new ApikitRuntimeException(e);
            }
        }
    }

    public Map<String, Flow> getRawRestFlowMap()
    {
        return restFlowMap;
    }

    private String getRestFlowKey(String name)
    {
        String[] coords = name.split(":");
        String[] methods = {"get", "put", "post", "delete", "head", "patch", "options"};
        if (coords.length < 2 || !Arrays.asList(methods).contains(coords[0]))
        {
            return null;
        }
        if (coords.length == 3 && !coords[2].equals(getName()))
        {
            return null;
        }
        return coords[0] + ":" + coords[1];
    }

    @Override
    protected FlowResolver getFlowResolver(AbstractConfiguration abstractConfiguration, String key)
    {
        return new RouterFlowResolver((Configuration) abstractConfiguration, key);
    }

    private static class RouterFlowResolver implements FlowResolver
    {

        private static final String WRAPPER_FLOW_SUFFIX = "-gateway-wrapper";

        private Map<String, Flow> restFlowMap;
        private String key;
        private Flow targetFlow;
        private Flow wrapperFlow;

        RouterFlowResolver(Configuration configuration, String key)
        {
            this.restFlowMap = configuration.restFlowMap;
            this.key = key;
            this.targetFlow = restFlowMap.get(key);
        }

        @Override
        public Flow getFlow()
        {
            //already wrapped
            if (wrapperFlow != null)
            {
                return wrapperFlow;
            }

            //target not implemented
            if (targetFlow == null)
            {
                return null;
            }

            //wrap target
            wrapperFlow = wrapFlow();
            restFlowMap.put(key, wrapperFlow);
            return wrapperFlow;
        }

        private Flow wrapFlow()
        {
            String flowName = targetFlow.getName() + WRAPPER_FLOW_SUFFIX;
            MuleContext muleContext = targetFlow.getMuleContext();
            Flow wrapper = new Flow(flowName, muleContext);
            wrapper.setMessageProcessors(Collections.<MessageProcessor>singletonList(new MessageProcessor()
            {
                @Override
                public MuleEvent process(MuleEvent muleEvent) throws MuleException
                {
                    return targetFlow.process(muleEvent);
                }
            }));
            try
            {
                muleContext.getRegistry().registerFlowConstruct(wrapper);
            }
            catch (MuleException e)
            {
                throw new RuntimeException("Error registering flow " + flowName, e);
            }
            return wrapper;
        }
    }
}
