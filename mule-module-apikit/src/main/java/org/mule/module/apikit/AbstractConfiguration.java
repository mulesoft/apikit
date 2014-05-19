/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.raml.parser.rule.ValidationResult.Level.ERROR;
import static org.raml.parser.rule.ValidationResult.Level.WARN;
import static org.raml.parser.rule.ValidationResult.UNKNOWN;

import org.mule.api.MuleContext;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.util.BeanUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.raml.emitter.RamlEmitter;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.NodeRuleFactory;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfiguration
{
    public static final String APPLICATION_RAML = "application/raml+yaml";
    public static final String BIND_ALL_HOST = "0.0.0.0";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected FlowConstruct flowConstruct;
    protected MuleContext muleContext;
    protected String raml;
    protected Raml api;
    private String baseHost;
    private Map<String, String> apikitRaml = new ConcurrentHashMap<String, String>();
    private boolean disableValidations;
    protected Map<String, FlowResolver> restFlowMapWrapper;

    public void loadApiDefinition(MuleContext muleContext, FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
        this.muleContext = muleContext;
        ResourceLoader loader = getRamlResourceLoader();
        initializeRestFlowMap();
        validateRaml(loader);
        RamlDocumentBuilder builder = new RamlDocumentBuilder(loader);
        api = builder.build(raml);
        injectEndpointUri(api);
        apikitRaml = new ConcurrentHashMap<String, String>();
        apikitRaml.put(baseHost, new RamlEmitter().dump(api));
        initializeRestFlowMapWrapper();
    }

    protected abstract void initializeRestFlowMap();

    protected void validateRaml(ResourceLoader resourceLoader)
    {
        NodeRuleFactory ruleFactory = getValidatorNodeRuleFactory();
        List<ValidationResult> results = RamlValidationService.createDefault(resourceLoader, ruleFactory).validate(raml);
        List<ValidationResult> errors = ValidationResult.getLevel(ERROR, results);
        if (!errors.isEmpty())
        {
            String msg = aggregateMessages(errors, "Invalid API descriptor -- errors found: ");
            throw new ApikitRuntimeException(msg);
        }
        List<ValidationResult> warnings = ValidationResult.getLevel(WARN, results);
        if (!warnings.isEmpty())
        {
            logger.warn(aggregateMessages(warnings, "API descriptor Warnings -- warnings found: "));
        }
    }

    private String aggregateMessages(List<ValidationResult> results, String header)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(header).append(results.size()).append("\n\n");
        for (ValidationResult result : results)
        {
            sb.append(result.getMessage()).append(" -- ");
            sb.append(" file: ");
            sb.append(result.getIncludeName() != null ? result.getIncludeName() : raml);
            if (result.getLine() != UNKNOWN)
            {
                sb.append(" -- line ");
                sb.append(result.getLine());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    protected abstract NodeRuleFactory getValidatorNodeRuleFactory();

    protected abstract ResourceLoader getRamlResourceLoader();

    private void injectEndpointUri(Raml ramlApi)
    {
        String address = getEndpointAddress(flowConstruct);
        ramlApi.setBaseUri(address);
        try
        {
            baseHost = new URI(address).getHost();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getEndpointAddress(FlowConstruct flowConstruct)
    {
        ImmutableEndpoint endpoint = (ImmutableEndpoint) ((Flow) flowConstruct).getMessageSource();
        String address = endpoint.getAddress();
        String path = endpoint.getEndpointURI().getPath();
        String scheme = endpoint.getEndpointURI().getScheme();
        String chAddress = System.getProperty("fullDomain");
        String chBaseUri = scheme + "://" + chAddress + path;
        if (logger.isDebugEnabled())
        {
            if (api != null)
            {
                logger.debug("yaml baseUri: " + api.getBaseUri());
            }
            logger.debug("mule baseUri: " + address);
            logger.debug("chub baseUri: " + chBaseUri);
        }
        if (chAddress != null)
        {
            address = chBaseUri;
        }
        if (address.endsWith("/"))
        {
            logger.debug("removing trailing slash from baseuri -> " + address);
            address = address.substring(0, address.length() - 1);
        }
        return address;
    }

    /**
     * Returns the RAML descriptor of the API.
     * If the baseUri is bound to all interfaces (0.0.0.0) the host parameter
     * is used to rewrite the base uri with the actual host received.
     */
    public String getApikitRaml(String host)
    {
        if (!BIND_ALL_HOST.equals(baseHost))
        {
            return apikitRaml.get(baseHost);
        }

        String hostRaml = apikitRaml.get(host);
        if (hostRaml == null)
        {
            Raml clone = cloneRaml(api);
            clone.setBaseUri(api.getBaseUri().replace(baseHost, host));
            hostRaml = new RamlEmitter().dump(clone);
            apikitRaml.put(host, hostRaml);
        }
        return hostRaml;
    }

    private Raml cloneRaml(Raml source)
    {
        try
        {
            return (Raml) BeanUtils.cloneBean(source);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isDisableValidations()
    {
        return disableValidations;
    }

    public void setDisableValidations(boolean disableValidations)
    {
        this.disableValidations = disableValidations;
    }

    public Raml getApi()
    {
        return api;
    }

    public Map<String, FlowResolver> getRestFlowMap()
    {
        return restFlowMapWrapper;
    }

    public MuleContext getMuleContext()
    {
        return muleContext;
    }

    protected void initializeRestFlowMapWrapper()
    {
        restFlowMapWrapper = populateFlowMapWrapper();
    }

    private Map<String, FlowResolver> populateFlowMapWrapper()
    {
        Map<String, FlowResolver> map = new HashMap<String, FlowResolver>();
        populateMapKeys(map, api.getResources());
        return map;
    }

    private void populateMapKeys(Map<String, FlowResolver> wrapperFlowMap, Map<String, Resource> resources)
    {
        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet())
        {
            String resource = resourceEntry.getKey();
            for (Map.Entry<ActionType, Action> actionEntry : resourceEntry.getValue().getActions().entrySet())
            {
                String key = actionEntry.getKey().name().toLowerCase() + ":" + resource;
                wrapperFlowMap.put(key, getFlowResolver(this, key));
            }
            populateMapKeys(wrapperFlowMap, resourceEntry.getValue().getResources());
        }
    }

    protected abstract FlowResolver getFlowResolver(AbstractConfiguration abstractConfiguration, String key);

}
