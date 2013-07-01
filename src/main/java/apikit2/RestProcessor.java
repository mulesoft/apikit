package apikit2;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.rest.uri.ResolvedVariables;
import org.mule.module.apikit.rest.uri.URIPattern;
import org.mule.module.apikit.rest.uri.URIResolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import apikit2.exception.InvalidUriParameterException;
import apikit2.exception.MethodNotAllowedException;
import apikit2.exception.MuleRestException;
import apikit2.exception.NotFoundException;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.visitor.YamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestProcessor implements MessageProcessor, Initialisable, MuleContextAware
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int URI_CACHE_SIZE = 1000;

    private MuleContext muleContext;
    private String config;
    private Raml api;
    private Map<String, RestFlow> restFlowMap;
    private Map<URIPattern, Resource> routingTable;
    private LoadingCache<String, URIResolver> uriResolverCache;
    private LoadingCache<String, URIPattern> uriPatternCache;

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
        InputStream ramlStream = this.getClass().getClassLoader().getResourceAsStream(config);
        //TODO perform validation
        api = builder.build(ramlStream);
        //TODO save post-processed yaml to serve to clients
    }

    private void buildRoutingTable(Map<String, Resource> resources)
    {
        for (Resource resource : resources.values())
        {
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
}
