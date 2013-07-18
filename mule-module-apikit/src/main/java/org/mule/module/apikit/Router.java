package org.mule.module.apikit;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.config.MuleProperties;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.registry.RegistrationException;
import org.mule.config.i18n.MessageFactory;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.InvalidUriParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.transport.http.HttpConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.YamlDocumentBuilder;
import org.raml.parser.visitor.YamlDocumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class Router implements MessageProcessor, Initialisable, MuleContextAware, FlowConstructAware
{

    public static final String APPLICATION_RAML = "application/raml+yaml";
    private static final int URI_CACHE_SIZE = 1000;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MuleContext muleContext;
    private FlowConstruct flowConstruct;
    private Configuration config;
    private Raml api;
    private Map<String, Flow> restFlowMap;
    private Map<URIPattern, Resource> routingTable;
    private LoadingCache<String, URIResolver> uriResolverCache;
    private LoadingCache<String, URIPattern> uriPatternCache;
    private String ramlYaml;
    private ConsoleHandler consoleHandler;

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        //avoid spring initialization
        if (flowConstruct == null)
        {
            return;
        }
        if (config == null)
        {
            try
            {
                config = muleContext.getRegistry().lookupObject(Configuration.class);
            }
            catch (RegistrationException e)
            {
                throw new InitialisationException(MessageFactory.createStaticMessage("APIKit configuration not Found"), this);
            }
        }

        loadApiDefinition();
        loadRestFlowMap();
        consoleHandler = new ConsoleHandler(api.getBaseUri(), config.getConsolePath());

        routingTable = new HashMap<URIPattern, Resource>();
        buildRoutingTable(api.getResources());

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

    private void loadRestFlowMap()
    {
        restFlowMap = new HashMap<String, Flow>();
        Collection<Flow> flows = muleContext.getRegistry().lookupObjects(Flow.class);
        for (Flow flow : flows)
        {
            String key = getRestFlowKey(flow.getName());
            if (key != null)
            {
                restFlowMap.put(key, flow);
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

    private String getRestFlowKey(String name)
    {
        String[] coords = name.split(":");
        String[] methods = {"get", "put", "post", "delete", "head"};
        if (coords.length < 2 || !Arrays.asList(methods).contains(coords[0]))
        {
            return null;
        }
        if (coords.length == 3 && !coords[2].equals(config.getName()))
        {
            return null;
        }
        return coords[0] + ":" + coords[1];
    }

    private void loadApiDefinition()
    {
        ResourceLoader loader = new AppHomeResourceLoader(muleContext);
        InputStream ramlStream = loader.fetchResource(config.getRaml());
        if (ramlStream == null)
        {
            logger.info("Raml descriptor not found in APP home directory. Trying the class path...");
            ramlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config.getRaml());
        }
        if (ramlStream == null)
        {
            throw new ApikitRuntimeException(String.format("RAML descriptor %s not found", config.getRaml()));
        }

        CompositeResourceLoader customLoader = new CompositeResourceLoader(loader, new DefaultResourceLoader());
        String ramlBuffer;
        try
        {
            ramlBuffer = IOUtils.toString(ramlStream);
        }
        catch (IOException e)
        {
            throw new ApikitRuntimeException(String.format("Cannot read RAML descriptor %s", config.getRaml()));
        }

        //TODO enable validations
        //validateRaml(ramlBuffer, customLoader);
        YamlDocumentBuilder<Raml> builder = new YamlDocumentBuilder<Raml>(Raml.class, customLoader);
        api = builder.build(ramlBuffer);
        injectEndpointUri(builder);
        ramlYaml = YamlDocumentBuilder.dumpFromAst(builder.getRootNode());
    }

    protected void validateRaml(String ramlBuffer, ResourceLoader resourceLoader)
    {
        YamlDocumentValidator validator = new YamlDocumentValidator(Raml.class, resourceLoader);
        List<ValidationResult> results = validator.validate(ramlBuffer);
        if (!results.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid RAML: ").append(results.size()).append(" errors found\n\n");
            for (ValidationResult result : results)
            {
                sb.append(result.getMessage()).append(" -- ");
                sb.append(result.getStartMark()).append("\n");
            }
            throw new ApikitRuntimeException(sb.toString());
        }
    }

    private void injectEndpointUri(YamlDocumentBuilder<Raml> builder)
    {
        ImmutableEndpoint endpoint = (ImmutableEndpoint) ((Flow) flowConstruct).getMessageSource();
        String address = endpoint.getAddress();
        String path = endpoint.getEndpointURI().getPath();
        String scheme = endpoint.getEndpointURI().getScheme();
        String chAddress = System.getProperty("fullDomain");
        String chBaseUri = scheme + "://" + chAddress + path;
        if (logger.isDebugEnabled())
        {
            logger.debug("raml baseUri: " + api.getBaseUri());
            logger.debug("mule baseUri: " + address);
            logger.debug("chub baseUri: " + chBaseUri);
        }
        if (chAddress != null)
        {
            address = chBaseUri;
        }
        api.setBaseUri(address);
        List<NodeTuple> tuples = new ArrayList<NodeTuple>();
        for (NodeTuple tuple : builder.getRootNode().getValue())
        {
            if (((ScalarNode) tuple.getKeyNode()).getValue().equals("baseUri"))
            {
                ScalarNode valueNode = (ScalarNode) tuple.getValueNode();
                tuples.add(new NodeTuple(tuple.getKeyNode(), new ScalarNode(valueNode.getTag(), address, valueNode.getStartMark(), valueNode.getEndMark(), valueNode.getStyle())));
            }
            else
            {
                tuples.add(tuple);
            }
        }
        builder.getRootNode().setValue(tuples);
    }

    private void buildRoutingTable(Map<String, Resource> resources)
    {
        for (Resource resource : resources.values())
        {
            String parentUri = resource.getParentUri();
            if (parentUri.contains("{version}"))
            {
                resource.setParentUri(parentUri.replaceAll("\\{version}", api.getVersion()));
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

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        HttpRestRequest request = new HttpRestRequest(event, api);

        String path = request.getResourcePath();

        //check for console request
        if (path.startsWith(api.getUri() + "/" + config.getConsolePath()))
        {
            if (config.isConsoleEnabled())
            {
                return consoleHandler.process(event);
            }
            else
            {
                throw new NotFoundException("console disabled");
            }
        }

        //check for raml descriptor request
        if (path.equals(api.getUri()) &&
            ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
            request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML))
        {
            event.getMessage().setPayload(ramlYaml);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, APPLICATION_RAML);
            event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, ramlYaml.length());
            return event;
        }

        URIPattern uriPattern;
        URIResolver uriResolver;
        try
        {
            uriPattern = uriPatternCache.get(path);
            uriResolver = uriResolverCache.get(path);
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof MuleRestException)
            {
                throw (MuleRestException) e.getCause();
            }
            throw new DefaultMuleException(e);
        }

        Resource resource = routingTable.get(uriPattern);
        if (resource.getAction(request.getMethod()) == null)
        {
            throw new MethodNotAllowedException(resource.getUri(), request.getMethod());
        }

        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);

        processUriParameters(resolvedVariables, resource, event);

        Flow flow = getFlow(resource, request.getMethod());
        if (flow == null)
        {
            throw new ApikitRuntimeException("Flow not found for resource: " + resource);
        }
        return request.process(flow, resource.getAction(request.getMethod()));
    }

    private void processUriParameters(ResolvedVariables resolvedVariables, Resource resource, MuleEvent event) throws InvalidUriParameterException
    {
        if (logger.isDebugEnabled())
        {
            for (String name : resolvedVariables.names())
            {
                logger.debug("        uri parameter: " + name + "=" + resolvedVariables.get(name));
            }
        }

        for (String key : resource.getUriParameters().keySet())
        {
            String value = (String) resolvedVariables.get(key);
            if (!resource.getUriParameters().get(key).validate(value))
            {
                throw new InvalidUriParameterException("Invalid uri parameter value " + value + " for " + key);
            }

        }
        for (String name : resolvedVariables.names())
        {
            event.getMessage().setInvocationProperty(name, resolvedVariables.get(name));
        }
    }

    private Flow getFlow(Resource resource, String method)
    {
        return restFlowMap.get(method + ":" + resource.getUri());
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    private static class AppHomeResourceLoader implements ResourceLoader
    {
        protected final Logger logger = LoggerFactory.getLogger(getClass());
        private final MuleContext muleContext;

        public AppHomeResourceLoader(MuleContext muleContext)
        {
            this.muleContext = muleContext;
        }

        @Override
        public InputStream fetchResource(String resourceName)
        {
            InputStream ramlStream = null;
            String appHome = muleContext.getRegistry().get(MuleProperties.APP_HOME_DIRECTORY_PROPERTY);
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Looking for resource: %s on app.home: %s...", resourceName, appHome));
            }
            File ramlFile = new File(appHome, resourceName);
            try
            {
                ramlStream = new FileInputStream(ramlFile);
            }
            catch (FileNotFoundException e)
            {
                logger.info(String.format("Resource: %s Not Found on app.home: %s...", resourceName, appHome));
            }
            return ramlStream;
        }
    }
}
