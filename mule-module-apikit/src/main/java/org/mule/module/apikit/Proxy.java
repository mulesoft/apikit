/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.StartException;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;
import org.mule.raml.interfaces.model.IResource;
import org.mule.util.WildcardAttributeEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will be removed on the next major version
 */
@Deprecated
public class Proxy extends AbstractRouter
{

    public static final Set<String> MULE_REQUEST_HEADERS;
    public static final Set<String> MULE_RESPONSE_HEADERS;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private Flow basicFlow;

    private static final String REMOVE_HEADERS_VARIABLE_NAME = "_headersToIgnore";

    static
    {
        String[] headers =
                {
                        "http.context.path",
                        "http.context.uri",
                        "http.headers",
                        "http.listener.path",
                        "http.method",
                        "http.query.params",
                        "http.query.string",
                        "http.relative.path",
                        "http.remote.address",
                        "http.request",
                        "http.request.path",
                        "http.request.uri",
                        "http.scheme",
                        "http.status",
                        "http.uri.params",
                        "http.version",
                        "server",
                        "x-mule_encoding",
                        "x-mule_session",
                        "mule_originating_endpoint",
                        "mule_remote_client_address",
                        "host",
                        "content-length",
                        "connection",
                        "transfer-encoding",
                        "server"
                };
        MULE_RESPONSE_HEADERS = new HashSet<>(Arrays.asList(headers));
        MULE_RESPONSE_HEADERS.remove("http.status");
        MULE_REQUEST_HEADERS = new HashSet<>(MULE_RESPONSE_HEADERS);
        MULE_REQUEST_HEADERS.remove("http.method");
    }

    public Proxy()
    {
        LOGGER.warn("Proxy class is deprecated and will be removed on the next major version");
    }

    public ProxyConfiguration getConfig()
    {
        return (ProxyConfiguration) config;
    }

    public void setConfig(ProxyConfiguration config)
    {
        this.config = config;
    }

    @Override
    protected void startConfiguration() throws StartException
    {
        if (config == null)
        {
            try
            {
                config = muleContext.getRegistry().lookupObject(ProxyConfiguration.class);
            }
            catch (RegistrationException e)
            {
                throw new StartException(MessageFactory.createStaticMessage("APIKit Proxy configuration not Found"), this);
            }
        }

        ((ProxyConfiguration) config).setChain(next);

        if (basicFlow == null)
        {
            config.initializeRestFlowMapWrapper();
            config.loadApiDefinition(flowConstruct);
            basicFlow = buildBasicFlow();
        }
    }

    private Flow buildBasicFlow()
    {
        String flowName = "__intercepted_chain_flow_" + config.getName();
        Flow wrapper = new Flow(flowName, muleContext);
        wrapper.setMessageProcessors(Collections.singletonList(next));
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

    @Override
    protected MuleEvent handleEvent(MuleEvent event, String path) throws MuleException
    {
        copyProperties(event, MULE_REQUEST_HEADERS);
        event.getMessage().setOutboundProperty("http.disable.status.code.exception.check", "true");
        return null;
    }

    @Override
    protected Flow getFlow(IResource resource, HttpRestRequest request, String version)
    {
        FlowResolver flowResolver = config.getRestFlowMap().get(request.getMethod() + ":" + resource.getResolvedUri(version));
        Flow rawFlow = ((ProxyConfiguration.ProxyFlowResolver) flowResolver).getRawFlow();
        if (rawFlow == null)
        {
            rawFlow = basicFlow;
        }
        return rawFlow;
    }

    @Override
    protected MuleEvent doProcessRouterResponse(MuleEvent event, Integer successStatus)
    {
        Proxy.copyProperties(event, Proxy.MULE_RESPONSE_HEADERS);
        return event;
    }

    public static void copyProperties(MuleEvent event, Set<String> skip)
    {
        List<String> headersToIgnore = new ArrayList<>();
        MuleMessage message = event.getMessage();
        Set<String> inboundPropertyNames = message.getInboundPropertyNames();
        List<String> candidatesToIgnore =  (ArrayList<String>)message.removeProperty(REMOVE_HEADERS_VARIABLE_NAME, PropertyScope.INVOCATION);
        if (candidatesToIgnore != null)
        {
            headersToIgnore = wildcardsEvaluation(candidatesToIgnore, message.getPropertyNames(PropertyScope.INBOUND));
        }
        for (String name : inboundPropertyNames)
        {
            if (!skip.contains(name.toLowerCase()) && !headersToIgnore.contains(name.toLowerCase()))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(String.format("+++  copying header %s -> %s", name, message.getInboundProperty(name)));
                }
                message.setOutboundProperty(name, message.getInboundProperty(name));
            }
            else
            {
                LOGGER.debug(String.format("--- skipping header %s -> %s", name, message.getInboundProperty(name)));
            }
        }
    }

    private static List<String> wildcardsEvaluation (List<String> candidatesToIgnore, Collection<String> affectedProperties)
    {
        final List<String> headersToIgnore = new ArrayList<>();
        for (final String property : candidatesToIgnore)
        {
            WildcardAttributeEvaluator wildcardAttributeEvaluator = new WildcardAttributeEvaluator(property);
            if (wildcardAttributeEvaluator.hasWildcards())
            {
                wildcardAttributeEvaluator.processValues(affectedProperties, new WildcardAttributeEvaluator.MatchCallback()
                {
                    @Override
                    public void processMatch(String matchedValue)
                    {
                        headersToIgnore.add(matchedValue.toLowerCase());
                    }
                });
            }
            else
            {
                headersToIgnore.add(property.toLowerCase());
            }
        }
        return headersToIgnore;
    }

}
