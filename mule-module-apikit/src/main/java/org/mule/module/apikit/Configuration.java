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
import org.mule.api.processor.DynamicPipelineException;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.transform.ApikitResponseTransformer;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configuration extends AbstractConfiguration
{

    public static final String DEFAULT_CONSOLE_PATH = "console";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean consoleEnabled = true;
    private String consolePath = DEFAULT_CONSOLE_PATH;
    private List<FlowMapping> flowMappings = new ArrayList<FlowMapping>();
    private Map<String, Flow> restFlowMap;
    private Map<String, Flow> restFlowMapUnwrapped;
    private Map<String, IResource> flatResourceTree = new HashMap<>();

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
    protected HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return new HttpRestRequest(event, this);
    }

    protected void initializeRestFlowMap()
    {
        flattenResourceTree(getApi().getResources());

        if (restFlowMap == null)
        {
            restFlowMap = new HashMap<>();

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
                restFlowMap.put(mapping.getKey(), mapping.getFlow());
            }

            logMissingMappings();

            restFlowMapUnwrapped = new HashMap<>(restFlowMap);
        }
    }

    private void flattenResourceTree(Map<String, IResource> resources)
    {
        for (IResource resource : resources.values())
        {
            flatResourceTree.put(resource.getResolvedUri(api.getVersion()), resource);
            if (resource.getResources() != null)
            {
                flattenResourceTree(resource.getResources());
            }
        }
    }

    private void logMissingMappings()
    {
        for (IResource resource : flatResourceTree.values())
        {
            String fullResource = resource.getResolvedUri(api.getVersion());
            for (IAction action : resource.getActions().values())
            {
                String method = action.getType().name().toLowerCase();
                String key = method + ":" + fullResource;
                if (restFlowMap.get(key) != null)
                {
                    continue;
                }
                if (action.hasBody())
                {
                    for (String contentType : action.getBody().keySet())
                    {
                        if (restFlowMap.get(key + ":" + contentType) == null)
                        {
                            logger.warn(String.format("Action-Resource-ContentType triplet has no implementation -> %s:%s:%s ",
                                                      method, fullResource, contentType));
                        }
                    }
                }
                else
                {
                    logger.warn(String.format("Action-Resource pair has no implementation -> %s:%s ",
                                              method, fullResource));
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("==== RestFlows defined:");
            for (String key : restFlowMap.keySet())
            {
                logger.debug("\t\t" + key);
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

    @Override
    public Set<String> getFlowActionRefs(Flow flow)
    {
        Set<String> flowActionRefs = super.getFlowActionRefs(flow);
        for (Map.Entry<String, Flow> entry : restFlowMapUnwrapped.entrySet())
        {
            if (flow == entry.getValue())
            {
                flowActionRefs.add(entry.getKey());
            }
        }
        return flowActionRefs;
    }

    public Map<String, Flow> getRawRestFlowMap()
    {
        return restFlowMap;
    }

    /**
     * validates if name is a valid router flow name according to the following pattern:
     *  method:/resource[:content-type][:config-name]
     *
     * @param name to be validated
     * @return the name with the config-name stripped or null if it is not a router flow
     */
    private String getRestFlowKey(String name)
    {
        String[] coords = name.split(":");
        String[] methods = {"get", "put", "post", "delete", "head", "patch", "options"};
        if (coords.length < 2 || coords.length > 4 ||
            !Arrays.asList(methods).contains(coords[0]) ||
            !coords[1].startsWith("/"))
        {
            return null;
        }
        if (coords.length == 4)
        {
            if (coords[3].equals(getName()))
            {
                return validateRestFlowKeyAgainstApi(coords[0], coords[1], coords[2]);
            }
            return null;
        }
        if (coords.length == 3)
        {
            if (coords[2].equals(getName()))
            {
                return validateRestFlowKeyAgainstApi(coords[0], coords[1]);
            }
            return validateRestFlowKeyAgainstApi(coords[0], coords[1], coords[2]);
        }
        return validateRestFlowKeyAgainstApi(coords[0], coords[1]);
    }

    private String validateRestFlowKeyAgainstApi(String... coords)
    {
        String method = coords[0];
        String resource = coords[1];
        String type = coords.length == 3 ? coords[2] : null;
        String key = String.format("%s:%s", method, resource);
        if (type != null)
        {
            key = key + ":" + type.toLowerCase();
        }
        IResource apiResource = flatResourceTree.get(resource);
        if (apiResource != null)
        {
            IAction action = apiResource.getAction(method);
            if (action != null)
            {
                if (type == null)
                {
                    return key;
                }
                else
                {
                    if (action.hasBody() && action.getBody().get(type) != null)
                    {
                        return key;
                    }
                }
            }
        }
        if (flowConfigPointsToCurrentConfig(type)) {
            logger.warn(String.format("Flow named \"%s\" does not match any RAML descriptor resource", key));
        }
        return null;
    }

    private boolean flowConfigPointsToCurrentConfig(String type) {
        return type != null && type.equals(getName());
    }

    @Override
    protected FlowResolver getFlowResolver(AbstractConfiguration abstractConfiguration, String key)
    {
        return new RouterFlowResolver((Configuration) abstractConfiguration, key);
    }

    @Override
    protected Map<String, FlowResolver> populateFlowMapWrapper()
    {
        Map<String, FlowResolver> map = new HashMap<>();
        for (Map.Entry<String, Flow> entry : restFlowMap.entrySet())
        {
            map.put(entry.getKey(), getFlowResolver(this, entry.getKey()));
        }
        return map;
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
            String flowName = key + ":" + targetFlow.getName() + WRAPPER_FLOW_SUFFIX;
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
                if (!wrapper.isStarted())
                {
                    wrapper.start();
                }
            }
            catch (MuleException e)
            {
                throw new RuntimeException("Error registering flow " + flowName, e);
            }
            return wrapper;
        }
    }

    @Override
    public void start() throws MuleException
    {
        addResponseTransformers(restFlowMap.values());
    }
}
