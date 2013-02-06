package org.mule.module.wsapi.rest;

import org.mule.VoidMuleEvent;
import org.mule.api.DefaultMuleException;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.rest.action.RestActionNotAllowedException;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapter;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapterFactory;
import org.mule.module.wsapi.rest.resource.RestResource;
import org.mule.module.wsapi.rest.resource.RestResourceNotFoundException;
import org.mule.module.wsapi.rest.uri.ResolvedVariables;
import org.mule.module.wsapi.rest.uri.URIPattern;
import org.mule.module.wsapi.rest.uri.URIResolver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestMessageProcessor implements MessageProcessor
{

    private static final int URI_CACHE_SIZE = 1000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RestWebServiceInterface restInterface;
    private Map<URIPattern, RestResource> routingTable;
    private LoadingCache<String, URIResolver> uriResolverCache;
    private LoadingCache<String, URIPattern> uriPatternCache;

    public RestMessageProcessor(RestWebServiceInterface restInterface)
    {
        this.restInterface = restInterface;
        routingTable = new HashMap<URIPattern, RestResource>();
        logger.debug("Creating REST resource hierarchy and updating routing table...");
        buildRoutingTable((List<RestResource>) restInterface.getRoutes());
        buildResourceCache();
    }

    private void buildResourceCache()
    {
        logger.info("Building resource URI cache...");
        uriResolverCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIResolver>() {
                            public URIResolver load(String path) throws IOException
                            {
                                return new URIResolver(path);
                            }
                        });

        uriPatternCache = CacheBuilder.newBuilder()
                .maximumSize(URI_CACHE_SIZE)
                .build(
                        new CacheLoader<String, URIPattern>() {
                            public URIPattern load(String path) throws Exception {
                                URIResolver resolver = uriResolverCache.get(path);
                                Collection<URIPattern> matches = resolver.findAll(routingTable.keySet());

                                if (matches.size() == 0) {
                                    logger.warn("No matching patterns for URI " + path);
                                    return null;
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(matches.size() + " matching patterns for URI " + path + ". Finding best one...");
                                    }
                                    for (URIPattern p : matches) {
                                        boolean best = (p == resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH));

                                        if (best) {
                                            return p;
                                        }
                                    }

                                    return null;
                                }
                            }
                        });
    }

    protected void buildRoutingTable(List<RestResource> resources)
    {
        for (RestResource resource : resources)
        {
            String uriPattern = resource.getTemplateUri();
            if (!uriPattern.startsWith("/"))
            {
                uriPattern = "/" + uriPattern;
            }
            logger.debug("Adding URI to the routing table: " + uriPattern);
            routingTable.put(new URIPattern(uriPattern), resource);
            if (resource.getResources() != null)
            {
                buildRoutingTable(resource.getResources());
            }
        }
    }

    @Override
    public MuleEvent process(MuleEvent muleEvent) throws MuleException
    {
        RestProtocolAdapter protocolAdapter = getProtocolAdapter(muleEvent);

        String path = protocolAdapter.getURI().getPath();

        URIPattern uriPattern;
        try
        {
            uriPattern = resolveVariables(muleEvent, path);
        }
        catch (Exception e)
        {
            muleEvent.getMessage().setOutboundProperty("http.status", 404);
            throw new RestResourceNotFoundException("Resource not found: " + path, muleEvent);
        }

        try
        {
            RestResource resource = routingTable.get(uriPattern);
            resource.getAction(protocolAdapter.getActionType(), muleEvent).process(muleEvent);
        }
        catch (RestActionNotAllowedException rana) {
            muleEvent.getMessage().setOutboundProperty("http.status", 405);
        }

        return muleEvent;
    }

    protected RestProtocolAdapter getProtocolAdapter(MuleEvent muleEvent)
    {
        return RestProtocolAdapterFactory.getInstance().getAdapterForEvent(muleEvent, restInterface.isUseRelativePath());
    }

    protected URIPattern resolveVariables(MuleEvent muleEvent, String path) throws ExecutionException
    {
        URIPattern uriPattern = uriPatternCache.get(path);
        ResolvedVariables resolvedVariables = uriResolverCache.get(path).resolve(uriPattern);

        if (logger.isDebugEnabled())
        {
            if (resolvedVariables.names().size() > 0)
            {
                logger.debug("resolved variables");
                for (String name : resolvedVariables.names())
                {
                    logger.debug("        " + name + "=" + resolvedVariables.get(name));
                }
            }
        }

        for (String name : resolvedVariables.names())
        {
            muleEvent.getMessage().setInvocationProperty(name, resolvedVariables.get(name));
        }
        return uriPattern;
    }
}
