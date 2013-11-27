/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.raml.parser.rule.ValidationResult.Level.ERROR;
import static org.raml.parser.rule.ValidationResult.Level.WARN;
import static org.yaml.snakeyaml.nodes.Tag.STR;

import org.mule.api.MuleContext;
import org.mule.api.config.MuleProperties;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Raml;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.NodeRuleFactory;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class Configuration
{

    public static final String APPLICATION_RAML = "application/raml+yaml";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;
    private String raml;
    private boolean consoleEnabled;
    private String consolePath;
    private List<FlowMapping> flowMappings = new ArrayList<FlowMapping>();
    private FlowConstruct flowConstruct;
    private Raml api;
    private String apikitRaml;

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

    public Raml getApi()
    {
        return api;
    }

    public String getApikitRaml()
    {
        return apikitRaml;
    }

    public void loadApiDefinition(MuleContext muleContext, FlowConstruct flowConstruct, Map<String, Flow> restFlowMap)
    {
        this.flowConstruct = flowConstruct;
        ResourceLoader loader = new DefaultResourceLoader();
        String appHome = muleContext.getRegistry().get(MuleProperties.APP_HOME_DIRECTORY_PROPERTY);
        if (appHome != null)
        {
            loader = new CompositeResourceLoader(new FileResourceLoader(appHome), loader);
        }
        InputStream ramlStream = loader.fetchResource(getRaml());
        if (ramlStream == null)
        {
            throw new ApikitRuntimeException(String.format("API descriptor %s not found", getRaml()));
        }

        String ramlBuffer;
        try
        {
            ramlBuffer = IOUtils.toString(ramlStream);
        }
        catch (IOException e)
        {
            throw new ApikitRuntimeException(String.format("Cannot read API descriptor %s", getRaml()));
        }

        validateRaml(ramlBuffer, loader, restFlowMap);
        RamlDocumentBuilder builder = new RamlDocumentBuilder(loader);
        api = builder.build(ramlBuffer);
        injectEndpointUri(builder);
        apikitRaml = new RamlEmitter().dump(api);
    }


    protected void validateRaml(String ramlBuffer, ResourceLoader resourceLoader, Map<String, Flow> restFlowMap)
    {
        NodeRuleFactory ruleFactory = new NodeRuleFactory(new ActionImplementedRuleExtension(restFlowMap));
        List<ValidationResult> results = RamlValidationService.createDefault(resourceLoader, ruleFactory).validate(ramlBuffer);
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
            sb.append(result.getIncludeName() != null ? result.getIncludeName() : getRaml());
            if (result.getStartMark() != null)
            {
                sb.append(" -- ");
                sb.append(result.getStartMark());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void injectEndpointUri(RamlDocumentBuilder builder)
    {
        String address = getEndpointAddress(flowConstruct);
        api.setBaseUri(address);
        List<NodeTuple> tuples = new ArrayList<NodeTuple>();
        boolean baseUriPresent = false;
        for (NodeTuple tuple : builder.getRootNode().getValue())
        {
            if (((ScalarNode) tuple.getKeyNode()).getValue().equals("baseUri"))
            {
                ScalarNode valueNode = (ScalarNode) tuple.getValueNode();
                tuples.add(new NodeTuple(tuple.getKeyNode(), new ScalarNode(valueNode.getTag(), address, valueNode.getStartMark(), valueNode.getEndMark(), valueNode.getStyle())));
                baseUriPresent = true;
            }
            else
            {
                tuples.add(tuple);
            }
        }
        if (!baseUriPresent)
        {
            Node keyNode = new ScalarNode(STR, "baseUri", null, null, '0');
            Node valueNode = new ScalarNode(STR, address, null, null, '0');
            tuples.add(new NodeTuple(keyNode, valueNode));
        }
        builder.getRootNode().setValue(tuples);
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
            logger.debug("yaml baseUri: " + api.getBaseUri());
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

}
