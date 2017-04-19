/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.module.apikit.validation.AttributesValidatior;
import org.mule.module.apikit.validation.BodyValidator;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.processor.AbstractInterceptingMessageProcessor;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class Router extends AbstractInterceptingMessageProcessor
{
    @Inject
    private ApikitRegistry registry;

    private String configRef;

    private String name;



    public Event process(Event event) throws MuleException
    {
        Configuration config = registry.getConfiguration(getConfigRef());
        event = EventHelper.addVariable(event, config.getOutboundHeadersMapName(), new HashMap<>());
        event = EventHelper.addVariable(event, config.getHttpStatusVarName(), "200");

        HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

        String path = UrlUtils.getRelativePath(attributes);
        path = path.isEmpty() ? "/" : path;

        //Get uriPattern, uriResolver, and the resolvedVariables
        URIPattern uriPattern;
        URIResolver uriResolver;
        try {
            uriPattern = config.getUriPatternCache().get(path);
            uriResolver = config.getUriResolverCache().get(path);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MuleRestException) {
                throw (MuleRestException) e.getCause();
            }
            throw new DefaultMuleException(e);
        }
        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);
        IResource resource = getResource(config, attributes.getMethod().toLowerCase(), uriPattern);

        event = validateRequest(event, config, resource, attributes, resolvedVariables);
        String contentType = MessageHelper.getHeaderIgnoreCase(event.getMessage(), HeaderNames.CONTENT_TYPE);
        Flow flow = config.getFlowFinder().getFlow(resource,attributes.getMethod().toLowerCase(), contentType);
        return flow.process(event);
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

    public Event validateRequest(Event event, Configuration config, IResource resource, HttpRequestAttributes attributes, ResolvedVariables resolvedVariables) throws DefaultMuleException, MuleRestException
    {
        //Attributes validation
        AttributesValidatior attributesValidatior = new AttributesValidatior(config);
        attributes = attributesValidatior.validateAndAddDefaults(attributes, resource, resolvedVariables);
        Event newEvent = EventHelper.regenerateEvent(event, attributes);

        //Body validation
        BodyValidator bodyValidator = new BodyValidator(config,resource.getAction(attributes.getMethod().toLowerCase()));
        return EventHelper.regenerateEvent(event, bodyValidator.validate(newEvent.getMessage()));
    }

    private IResource getResource(Configuration configuration, String method, URIPattern uriPattern) throws MethodNotAllowedException
    {
        IResource resource = configuration.getFlowFinder().getResource(uriPattern);
        if (resource.getAction(method) == null) {
            throw new MethodNotAllowedException(resource.getUri(), method);
        }
        return resource;
    }



}
