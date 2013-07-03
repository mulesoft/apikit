package apikit2;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.module.apikit.rest.uri.ResolvedVariables;
import org.mule.module.apikit.rest.uri.URIPattern;
import org.mule.module.apikit.rest.uri.URIResolver;
import org.mule.transport.http.HttpConstants;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import apikit2.exception.ApikitRuntimeException;
import apikit2.exception.InvalidUriParameterException;
import apikit2.exception.MethodNotAllowedException;
import apikit2.exception.MuleRestException;
import apikit2.exception.NotFoundException;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.visitor.YamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class RestProcessor implements MessageProcessor, Initialisable, MuleContextAware, FlowConstructAware
{

    public static final String APPLICATION_RAML = "application/raml";
    private static final int URI_CACHE_SIZE = 1000;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private MuleContext muleContext;
    private FlowConstruct flowConstruct;
    private String config;
    private Raml api;
    private Map<String, RestFlow> restFlowMap;
    private Map<URIPattern, Resource> routingTable;
    private LoadingCache<String, URIResolver> uriResolverCache;
    private LoadingCache<String, URIPattern> uriPatternCache;
    private String ramlYaml;

    public void setConfig(String config)
    {
        this.config = config;
    }

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        //avoid spring initialization
        if (flowConstruct == null)
        {
            return;
        }

        loadApiDefinition();
        loadRestFlowMap();

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
        restFlowMap = new HashMap<String, RestFlow>();
        Collection<RestFlow> restFlows = muleContext.getRegistry().lookupObjects(RestFlow.class);
        for (RestFlow flow : restFlows)
        {
            restFlowMap.put(getBasePath() + flow.getResource() + flow.getAction(), flow);
        }
        //log map info
        if (logger.isDebugEnabled())
        {
            logger.debug("==== RestFlows defined:");
            for (String key : restFlowMap.keySet())
            {
                logger.debug("\t\t" + key);
            }
        }
    }

    private void loadApiDefinition()
    {
        YamlDocumentBuilder<Raml> builder = new YamlDocumentBuilder<Raml>(Raml.class);
        InputStream ramlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
        if (ramlStream == null)
        {
            throw new ApikitRuntimeException(String.format("RAML descriptor %s not found", config));
        }
        //TODO perform validation
        api = builder.build(ramlStream);
        injectEndpointUri(builder);
        ramlYaml = YamlDocumentBuilder.dumpFromAst(builder.getRootNode());
    }

    private void injectEndpointUri(YamlDocumentBuilder<Raml> builder)
    {
        String address = ((ImmutableEndpoint) ((Flow) flowConstruct).getMessageSource()).getAddress();
        if (logger.isDebugEnabled())
        {
            logger.debug("yaml baseUri: " + api.getBaseUri());
            logger.debug("mule baseUri: " + address);
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
        //RestProtocolAdapter protocolAdapter = RestProtocolAdapterFactory.getInstance().getAdapterForEvent(muleEvent, isUseRelativePath());
        HttpRestRequest request = new HttpRestRequest(event, api);

        //String path = protocolAdapter.getResourceUri().getPath();
        String path = request.getResourcePath();

        //check for raml descriptor request
        if (path.equals(api.getUri()) /*&&
            ActionType.GET.toString().equals(request.getMethod().toUpperCase()) &&
            request.getAdapter().getAcceptableResponseMediaTypes().contains(APPLICATION_RAML)*/)   //FIXME serve any content type
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

        RestFlow flow = getFlow(resource, request.getMethod());
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

    private RestFlow getFlow(Resource resource, String method)
    {
        return restFlowMap.get(resource.getUri() + method);
    }

    public String getBasePath()
    {
        URL url;
        try
        {
            url = new URL(api.getBaseUri());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        return url.getPath();
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
