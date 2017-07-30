/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.runtime.core.api.processor.MessageProcessors.processToApply;
import static org.mule.runtime.core.api.processor.MessageProcessors.processWithChildContext;
import static reactor.core.publisher.Flux.from;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.validation.RequestValidator;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.api.exception.MessagingException;
import org.mule.runtime.core.api.exception.TypedException;
import org.mule.runtime.core.api.processor.Processor;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Router implements  Processor, FlowConstructAware, Initialisable

{
    @Inject
    private ApikitRegistry registry;

    private String configRef;

    private String name;

    private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

    @Override
    public void initialise() throws InitialisationException
    {
        URI uri = MessageSourceUtils.getUriFromFlow((Flow) flowConstruct);
        if (uri == null)
        {
            LOGGER.error("There was an error retrieving api source. Console will work only if the keepRamlBaseUri property is set to true.");
            return;
        }
        registry.setApiSource(configRef, uri.toString().replace("*",""));
    }

    public Event process(final Event event) throws MuleException {
        return processToApply(event, this);
    }

    @Override
    public Publisher<Event> apply(Publisher<Event> publisher)
    {
        return from(publisher).flatMap(event -> {
            try
            {
                Configuration config = registry.getConfiguration(getConfigRef());
                Event.Builder eventBuilder = Event.builder(event);
                eventBuilder.addVariable(config.getOutboundHeadersMapName(), new HashMap<>());

                HttpRequestAttributes attributes = ((HttpRequestAttributes)event.getMessage().getAttributes().getValue());

                String path = UrlUtils.getRelativePath(attributes.getListenerPath(), attributes.getRequestPath());
                path = path.isEmpty() ? "/" : path;

                //Get uriPattern, uriResolver, and the resolvedVariables
                URIPattern uriPattern = config.getUriPatternCache().get(path);
                URIResolver uriResolver = config.getUriResolverCache().get(path);

                ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);
                IResource resource = getResource(config, attributes.getMethod().toLowerCase(), uriPattern);
                if (!config.isDisableValidations())
                {
                    eventBuilder = validateRequest(event, eventBuilder, config, resource, attributes, resolvedVariables);
                }
                String contentType = AttributesHelper.getMediaType(attributes);
                Flow flow = config.getFlowFinder().getFlow(resource, attributes.getMethod().toLowerCase(), contentType);
                String successStatusCode = config.getRamlHandler().getSuccessStatusCode(resource.getAction(attributes.getMethod().toLowerCase()));
                eventBuilder.addVariable(config.getHttpStatusVarName(), successStatusCode);
                return processWithChildContext(eventBuilder.build(), flow, Optional.empty());
            }
            catch (Exception e)
            {
                return Flux.error(new MessagingException(event, e));
            }
        });
    }

    public String getConfigRef()
    {
        return configRef;
    }

    public void setConfigRef(String config)
    {
        this.configRef = config;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Event.Builder validateRequest(Event event, Event.Builder eventbuilder, ValidationConfig config, IResource resource, HttpRequestAttributes attributes, ResolvedVariables resolvedVariables) throws DefaultMuleException, MuleRestException {

        String charset = null;
        try {
            charset = getEncoding(event.getMessage(), event.getMessage().getPayload().getValue(), LOGGER);
        } catch (IOException e) {
            throw ApikitErrorTypes.throwErrorType(new BadRequestException("Error processing request: " + e.getMessage()));
        }

        ValidRequest validRequest = RequestValidator.validate(config, resource, attributes, resolvedVariables, event.getMessage().getPayload().getValue(), charset);

        return EventHelper.regenerateEvent(event.getMessage(), eventbuilder, validRequest);
    }

    private IResource getResource(Configuration configuration, String method, URIPattern uriPattern) throws TypedException
    {
        IResource resource = configuration.getFlowFinder().getResource(uriPattern);
        if (resource.getAction(method) == null) {
            throw ApikitErrorTypes.throwErrorType(new MethodNotAllowedException(resource.getUri() + " : " + method));
        }
        return resource;
    }

    FlowConstruct flowConstruct;
    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
