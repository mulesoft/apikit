/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.UrlUtils.getBaseSchemeHostPort;
import static org.raml.parser.rule.ValidationResult.Level.ERROR;
import static org.raml.parser.rule.ValidationResult.Level.WARN;
import static org.raml.parser.rule.ValidationResult.UNKNOWN;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Startable;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.injector.RamlUpdater;
import org.mule.module.apikit.spi.RouterService;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.util.BeanUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.SerializationUtils;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfiguration implements Initialisable, MuleContextAware, Startable
{

    public static final String APPLICATION_RAML = "application/raml+yaml";
    private static final String CONSOLE_URL_FILE = "consoleurl";
    private static final int URI_CACHE_SIZE = 1000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected FlowConstruct flowConstruct;
    protected MuleContext muleContext;
    private String name;
    protected String raml;
    private Raml baseApi; //original raml
    protected Raml api; //current raml
    private String baseSchemeHostPort;
    private Map<String, String> apikitRaml = new ConcurrentHashMap<String, String>();
    private boolean disableValidations;
    protected Map<String, FlowResolver> restFlowMapWrapper;
    protected Map<URIPattern, Resource> routingTable;
    protected LoadingCache<String, URIResolver> uriResolverCache;
    protected LoadingCache<String, URIPattern> uriPatternCache;
    private List<String> consoleUrls = new ArrayList<String>();
    private boolean started;
    protected boolean extensionEnabled = false;
    private RouterService routerExtension = null;

    @Override
    public void initialise() throws InitialisationException
    {
        if (muleContext == null)
        {
            return;
        }

        ResourceLoader loader = getRamlResourceLoader();
        validateRaml(loader);
        RamlDocumentBuilder builder = new RamlDocumentBuilder(loader);
        api = builder.build(raml);
        cleanBaseUriParameters(api);
        baseSchemeHostPort = getBaseSchemeHostPort(api.getBaseUri());
        initializeRestFlowMap();
        initializeRestFlowMapWrapper();
        loadRoutingTable();
        buildResourcePatternCaches();
    }

    @Override
    public void start() throws MuleException
    {

    }

    private void loadRoutingTable()
    {
        if (routingTable == null)
        {
            routingTable = new ConcurrentHashMap<URIPattern, Resource>();
        }
        buildRoutingTable(getApi().getResources());
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
                                return new URIResolver(path);
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
                                URIPattern match = resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH);

                                if (match == null)
                                {
                                    logger.warn("No matching patterns for URI " + path);
                                    throw new NotFoundException(path);
                                }
                                return match;
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
        resetRamlMap();
    }

    public void updateApi(Raml newApi)
    {
        api = newApi;
        loadRoutingTable();
        resetRamlMap();
    }

    private void resetRamlMap()
    {
        apikitRaml = new ConcurrentHashMap<String, String>();
        apikitRaml.put(baseSchemeHostPort, new RamlEmitter().dump(api));
    }

    protected abstract void initializeRestFlowMap();

    protected void validateRaml(ResourceLoader resourceLoader)
    {
        List<ValidationResult> results = RamlValidationService.createDefault(resourceLoader).validate(raml);
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

    public abstract ResourceLoader getRamlResourceLoader();

    private void injectEndpointUri(Raml ramlApi)
    {
        String address = getEndpointAddress(flowConstruct);
        ramlApi.setBaseUri(address);
        baseSchemeHostPort = getBaseSchemeHostPort(address);
    }

    private void cleanBaseUriParameters(Raml ramlApi)
    {
        ramlApi.getBaseUriParameters().clear();
        cleanBaseUriParameters(ramlApi.getResources());
    }

    private void cleanBaseUriParameters(Map<String, Resource> resources)
    {
        for (Resource resource : resources.values())
        {
            resource.getBaseUriParameters().clear();
            for (Action action : resource.getActions().values())
            {
                action.getBaseUriParameters().clear();
            }
            if (!resource.getResources().isEmpty())
            {
                cleanBaseUriParameters(resource.getResources());
            }
        }
    }

    public String getEndpointAddress(FlowConstruct flowConstruct)
    {
        MessageSourceAdapter adapter = new MessageSourceAdapter(((Flow) flowConstruct).getMessageSource());
        String address = adapter.getAddress();
        String path = adapter.getPath();
        String scheme = adapter.getScheme();
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
     * The schemeHostPort parameter is used to rewrite the base uri with the actual host received.
     */
    public String getApikitRaml(String schemeHostPort)
    {
        if (schemeHostPort == null)
        {
            return apikitRaml.get(baseSchemeHostPort);
        }
        String hostRaml = apikitRaml.get(schemeHostPort);
        if (hostRaml == null)
        {
            Raml clone = shallowCloneRaml(api);
            clone.setBaseUri(api.getBaseUri().replace(baseSchemeHostPort, schemeHostPort));
            hostRaml = new RamlEmitter().dump(clone);
            apikitRaml.put(schemeHostPort, hostRaml);
        }
        return hostRaml;
    }

    public String getApikitRaml(MuleEvent event)
    {
        return getApikitRaml(getBaseSchemeHostPort(event));
    }

    /**
     * returns the raml descriptor using the host from the console request event
     * only when the bind to all interfaces ip (0.0.0.0) is used for the router endpoint.
     * Otherwise it uses the router endpoint address as it is
     */
    public String getApikitRamlConsole(MuleEvent event)
    {
        String schemeHostPort = baseSchemeHostPort;
        String bindAllInterfaces = "0.0.0.0";
        if (schemeHostPort.contains(bindAllInterfaces))
        {
            try
            {
                URL url = new URL(getBaseSchemeHostPort(event));
                schemeHostPort = schemeHostPort.replace(bindAllInterfaces, url.getHost());

            }
            catch (MalformedURLException e)
            {
                throw new ApikitRuntimeException(e);
            }
        }
        return getApikitRaml(schemeHostPort);
    }

    private Raml deepCloneRaml(Raml source)
    {
        Raml target = (Raml) SerializationUtils.deserialize(SerializationUtils.serialize(source));
        copyCompiledSchemas(source, target);
        return target;
    }

    private void copyCompiledSchemas(Raml source, Raml target)
    {
        target.setCompiledSchemas(source.getCompiledSchemas());
    }

    private Raml shallowCloneRaml(Raml source)
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
            url = url.replace("0.0.0.0", "127.0.0.1");
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

    protected HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return new HttpRestRequest(event, this);
    }

    public static Collection<AbstractConfiguration> getAllConfigurations(MuleContext muleContext)
    {
        Collection<AbstractConfiguration> configurations = Sets.newHashSet();
        configurations.addAll(muleContext.getRegistry().lookupObjects(Configuration.class));
        configurations.addAll(muleContext.getRegistry().lookupObjects(ProxyConfiguration.class));
        return configurations;
    }

    public RamlUpdater getRamlUpdater()
    {
        if (baseApi == null)
        {
            baseApi = deepCloneRaml(api);
        }
        return new RamlUpdater(deepCloneRaml(baseApi), this);
    }

    public Set<String> getFlowActionRefs(Flow flow)
    {
        Set<String> actionRefs = new HashSet<String>();
        for (Map.Entry<String, FlowResolver> entry : restFlowMapWrapper.entrySet())
        {
            if (flow == entry.getValue().getFlow())
            {
                actionRefs.add(entry.getKey());
            }
        }
        return actionRefs;
    }

    public boolean isExtensionEnabled()
    {
        return extensionEnabled;
    }

    public void setExtensionEnabled(boolean extensionEnabled)
    {
        if (extensionEnabled)
        {
            ServiceLoader<RouterService> loader = ServiceLoader.load(RouterService.class);
            Iterator<RouterService> it = loader.iterator();
            if (it.hasNext())
            {
                this.extensionEnabled = true;
                routerExtension = it.next();
            }
        }
    }

    public RouterService getRouterExtension()
    {
        return this.routerExtension;
    }
}
