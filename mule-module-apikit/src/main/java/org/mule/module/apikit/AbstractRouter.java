/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.google.common.cache.LoadingCache;
import org.mule.DefaultMuleEvent;
import org.mule.NonBlockingVoidMuleEvent;
import org.mule.OptimizedRequestContext;
import org.mule.VoidMuleEvent;
import org.mule.api.DefaultMuleException;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.NonBlockingSupported;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.lifecycle.StartException;
import org.mule.api.transport.NonBlockingReplyToHandler;
import org.mule.api.transport.ReplyToHandler;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.InvalidUriParameterException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mule.module.http.api.HttpConstants.RequestProperties.HTTP_URI_PARAMS;

public abstract class AbstractRouter extends AbstractInterceptingMessageProcessor implements ApiRouter, NonBlockingSupported
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected FlowConstruct flowConstruct;
    protected AbstractConfiguration config;
    protected RamlDescriptorHandler ramlHandler;

    @Override
    public void start() throws MuleException
    {
        startConfiguration();
        ramlHandler = new RamlDescriptorHandler(config);
        config.publishConsoleUrls(muleContext.getConfiguration().getWorkingDirectory());
    }

    protected abstract void startConfiguration() throws StartException;

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    @Override
    public final MuleEvent process(MuleEvent event) throws MuleException
    {
        if (isNonBlocking(event))
        {
            return processNonBlocking(event);
        }
        else
        {
            return processBlocking(event);
        }
    }

    private boolean isNonBlocking(MuleEvent event)
    {
        return event.isAllowNonBlocking() && event.getReplyToHandler() != null;
    }


    protected MuleEvent processBlocking(MuleEvent event) throws MuleException
    {
        if (config.isExtensionEnabled() && config.getRouterExtension().isExecutable(event))
        {
            return config.getRouterExtension().processBlockingRequest(event, this);
        }
        else
        {
            return processBlockingRequest(event);
        }
    }

    public MuleEvent processBlockingRequest(MuleEvent event) throws MuleException
    {
        RouterRequest result = processRouterRequest(event);
        event = result.getEvent();
        if (result.getFlow() != null)
        {
            event = result.getFlow().process(event);
        }
        return processRouterResponse(event, result.getSuccessStatus());
    }

    protected MuleEvent processNonBlocking(final MuleEvent request) throws MuleException
    {
        final RouterRequest result = processRouterRequest(request);

        MuleEvent event = result.getEvent();

        event = new DefaultMuleEvent(event, createReplyToHandler(request, result));
        // Update RequestContext ThreadLocal for backwards compatibility
        OptimizedRequestContext.unsafeSetEvent(event);

        if (result.getFlow() != null)
        {
            event = result.getFlow().process(event);
        }
        if (!(event instanceof NonBlockingVoidMuleEvent))
        {
            return processRouterResponse(event, result.getSuccessStatus());
        }
        return event;
    }

    private ReplyToHandler createReplyToHandler(final MuleEvent request,final RouterRequest result) {
        final ReplyToHandler originalReplyToHandler = request.getReplyToHandler();
        return new NonBlockingReplyToHandler()
        {
            @Override
            public void processReplyTo(MuleEvent event, MuleMessage returnMessage, Object replyTo) throws MuleException
            {
                try {
                    MuleEvent response = processRouterResponse(new DefaultMuleEvent(event, originalReplyToHandler), result.getSuccessStatus());
                    // Update RequestContext ThreadLocal for backwards compatibility
                    OptimizedRequestContext.unsafeSetEvent(response);
                    if (!NonBlockingVoidMuleEvent.getInstance().equals(response)) {
                        originalReplyToHandler.processReplyTo(response, null, null);
                    }
                } catch (Exception e)
                {
                    processExceptionReplyTo(new MessagingException(event, e), null);
                }
            }

            @Override
            public void processExceptionReplyTo(MessagingException exception, Object replyTo)
            {
                originalReplyToHandler.processExceptionReplyTo(exception, replyTo);
            }
        };
    }

    protected RouterRequest processRouterRequest(MuleEvent event) throws MuleException
    {
        HttpRestRequest request = getHttpRestRequest(event);

        String path = request.getResourcePath();

        //check for raml descriptor request
        if (ramlHandler.handles(request))
        {
            return new RouterRequest(ramlHandler.processRouterRequest(event));
        }

        MuleEvent handled = handleEvent(event, path);
        if (handled != null)
        {
            return new RouterRequest(handled);
        }

        URIPattern uriPattern;
        URIResolver uriResolver;
        path = path.isEmpty() ? "/" : path;
        try
        {
            uriPattern = getUriPatternCache().get(path);
            uriResolver = getUriResolverCache().get(path);
        }
        catch (ExecutionException e)
        {
            if (e.getCause() instanceof MuleRestException)
            {
                throw (MuleRestException) e.getCause();
            }
            throw new DefaultMuleException(e);
        }

        IResource resource = getRoutingTable().get(uriPattern);
        if (resource.getAction(request.getMethod()) == null)
        {
            throw new MethodNotAllowedException(resource.getResolvedUri(config.getApi().getVersion()), request.getMethod());
        }

        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);

        processUriParameters(resolvedVariables, resource, event);

        Flow flow = getFlow(resource, request, config.getApi().getVersion());
        if (flow == null)
        {
            throw new ApikitRuntimeException("Flow not found for resource: " + resource);
        }

        MuleEvent validatedEvent = request.validate(resource.getAction(request.getMethod()));

        return new RouterRequest(validatedEvent, flow, request.getSuccessStatus());
    }

    private MuleEvent processRouterResponse(MuleEvent event, Integer successStatus)
    {
        if (event == null || VoidMuleEvent.getInstance().equals(event))
        {
            return event;
        }
        return doProcessRouterResponse(event, successStatus);
    }

    protected abstract MuleEvent doProcessRouterResponse(MuleEvent event, Integer successStatus);


    @Override
    protected MuleEvent processNext(MuleEvent event) throws MuleException
    {
        throw new UnsupportedOperationException();
    }

    private Map<URIPattern, IResource> getRoutingTable()
    {
        return config.routingTable;
    }

    private LoadingCache<String, URIResolver> getUriResolverCache()
    {
        return config.uriResolverCache;
    }

    private LoadingCache<String, URIPattern> getUriPatternCache()
    {
        return config.uriPatternCache;
    }

    protected abstract MuleEvent handleEvent(MuleEvent event, String path) throws MuleException;

    private HttpRestRequest getHttpRestRequest(MuleEvent event)
    {
        return config.getHttpRestRequest(event);
    }

    private void processUriParameters(ResolvedVariables resolvedVariables, IResource resource, MuleEvent event) throws InvalidUriParameterException
    {
        if (logger.isDebugEnabled())
        {
            for (String name : resolvedVariables.names())
            {
                logger.debug("        uri parameter: " + name + "=" + resolvedVariables.get(name));
            }
        }

        if (!config.isDisableValidations())
        {
            for (Map.Entry<String, IParameter> entry : resource.getResolvedUriParameters().entrySet())
            {
                // version don't need validation
                if (entry.getKey().equals("version")) continue;
                String value = (String) resolvedVariables.get(entry.getKey());
                IParameter uriParameter = entry.getValue();
                if (!uriParameter.validate(value))
                {
                    String msg = String.format("Invalid value '%s' for uri parameter %s. %s",
                                               value, entry.getKey(), uriParameter.message(value));
                    throw new InvalidUriParameterException(msg);
                }
            }
        }

        Map<String, String> uriParams = new HashMap<>();
        for (String name : resolvedVariables.names())
        {
            String value = String.valueOf(resolvedVariables.get(name));
            event.getMessage().setInvocationProperty(name, value);
            uriParams.put(name, value);
        }
        if (event.getMessage().getInboundProperty(HTTP_URI_PARAMS) != null)
        {
            event.getMessage().<Map>getInboundProperty(HTTP_URI_PARAMS).putAll(uriParams);
        }
    }

    protected abstract Flow getFlow(IResource resource, HttpRestRequest request, String version) throws UnsupportedMediaTypeException;

    private static class RouterRequest
    {

        private MuleEvent event;
        private Flow flow;
        private Integer successStatus;

        public RouterRequest(MuleEvent event)
        {
            this(event, null, null);
        }

        public RouterRequest(MuleEvent event, Flow flow, Integer successStatus)
        {
            this.event = event;
            this.flow = flow;
            this.successStatus = successStatus;
        }

        public MuleEvent getEvent()
        {
            return event;
        }

        public Flow getFlow()
        {
            return flow;
        }

        public Integer getSuccessStatus()
        {
            return successStatus;
        }
    }
}
