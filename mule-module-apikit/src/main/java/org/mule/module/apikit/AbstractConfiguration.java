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
import org.mule.api.MuleEvent;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.MuleContextAware;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.util.BeanUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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

public abstract class AbstractConfiguration implements Initialisable, MuleContextAware
{

    public static final String APPLICATION_RAML = "application/raml+yaml";
    public static final String BIND_ALL_HOST = "0.0.0.0";
    private static final String CONSOLE_URL_FILE = "consoleurl";
    private static final int URI_CACHE_SIZE = 1000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected FlowConstruct flowConstruct;
    protected MuleContext muleContext;
    private String name;
    protected String raml;
    protected Raml api;
    private String baseHost;
    private Map<String, String> apikitRaml = new ConcurrentHashMap<String, String>();
    private boolean disableValidations;
    protected Map<String, FlowResolver> restFlowMapWrapper;
    protected Map<URIPattern, Resource> routingTable;
    protected LoadingCache<String, URIResolver> uriResolverCache;
    protected LoadingCache<String, URIPattern> uriPatternCache;
    private List<String> consoleUrls = new ArrayList<String>();
    private boolean started;

    @Override
    public void initialise() throws InitialisationException
    {
        if (muleContext == null)
        {
            return;
        }

        ResourceLoader loader = getRamlResourceLoader();
        initializeRestFlowMap();
        validateRaml(loader);
        RamlDocumentBuilder builder = new RamlDocumentBuilder(loader);
        api = builder.build(raml);
        initializeRestFlowMapWrapper();
        routingTable = new HashMap<URIPattern, Resource>();
        buildRoutingTable(getApi().getResources());
        buildResourcePatternCaches();
    }


    private void buildResourcePatternCaches()
    {
        logger.info("Building resource URI cache...");
        uriResolverCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIResolver>()
                        {
                            public URIResolver load(String path) throws IOException
                            {
                                return new URIResolver(URICoder.encode(path, '/'));
                            }
                        });

        uriPatternCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIPattern>()
                        {
                            public URIPattern load(String path) throws Exception
                            {
                                URIResolver resolver = uriResolverCache.get(path);
                                Collection<URIPattern> matches = resolver.findAll(routingTable.keySet());

                                if (matches.size() == 0)
                                {
                                    logger.warn("No matching patterns for URI " + path);
                                    throw new NotFoundException(path);
                                }
                                else
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug(matches.size() + " matching patterns for URI " + path + ". Finding best one...");
                                    }
                                    for (URIPattern p : matches)
                                    {
                                        boolean best = (p == resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH));

                                        if (best)
                                        {
                                            return p;
                                        }
                                    }

                                    return null;
                                }
                            }
                        });
    }

    private void buildRoutingTable(Map<String, Resource> resources)
    {
        for (Resource resource : resources.values())
        {
            String parentUri = resource.getParentUri();
            if (parentUri.contains("{version}"))
            {
                resource.setParentUri(parentUri.replaceAll("\\{version}", getApi().getVersion()));
            }
            String uri = resource.getUri();
            logger.debug("Adding URI to the routing table: " + uri);
            routingTable.put(new URIPattern(uri), resource);
            if (resource.getResources() != null)
            {
                buildRoutingTable(resource.getResources());
            }
        }
    }

    public void loadApiDefinition(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
        injectEndpointUri(api);
        apikitRaml = new ConcurrentHashMap<String, String>();
        apikitRaml.put(baseHost, new RamlEmitter().dump(api));
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

    public abstract ResourceLoader getRamlResourceLoader();

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

    public void setMuleContext(MuleContext muleContext)
    {
        this.muleContext = muleContext;
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
            String resource = resourceEntry.getValue().getUri();
            for (Map.Entry<ActionType, Action> actionEntry : resourceEntry.getValue().getActions().entrySet())
            {
                String key = actionEntry.getKey().name().toLowerCase() + ":" + resource;
                wrapperFlowMap.put(key, getFlowResolver(this, key));
            }
            populateMapKeys(wrapperFlowMap, resourceEntry.getValue().getResources());
        }
    }

    protected abstract FlowResolver getFlowResolver(AbstractConfiguration abstractConfiguration, String key);

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
        for (AbstractConfiguration configuration : getAllConfigurations(muleContext))
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
        for (AbstractConfiguration configuration : getAllConfigurations(muleContext))
        {
            urls.addAll(configuration.consoleUrls);
        }
        return urls;
    }

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

    public Action getEventAction(MuleEvent event)
    {
        HttpRestRequest request = getHttpRestRequest(event);
        String path = request.getResourcePath();
        URIPattern uriPattern;
        try
        {
            uriPattern = uriPatternCache.get(path);
        }
        catch (ExecutionException e)
        {
            return null;
        }
        Resource resource = routingTable.get(uriPattern);
        return resource.getAction(request.getMethod());
    }

    protected abstract HttpRestRequest getHttpRestRequest(MuleEvent muleEvent);

    public static Collection<AbstractConfiguration> getAllConfigurations(MuleContext muleContext)
    {
        Collection<AbstractConfiguration> configurations = new ArrayList<AbstractConfiguration>();
        configurations.addAll(muleContext.getRegistry().lookupObjects(Configuration.class));
        configurations.addAll(muleContext.getRegistry().lookupObjects(ProxyConfiguration.class));
        return configurations;
    }
}
