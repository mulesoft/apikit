/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.CharsetUtils.getEncoding;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.uri.ResolvedVariables;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.apikit.uri.URIResolver;
import org.mule.module.apikit.validation.RequestValidator;
import org.mule.module.apikit.validation.ValidRequest;
import org.mule.module.apikit.validation.ValidationConfig;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.exception.TypedException;
import org.mule.runtime.core.processor.AbstractInterceptingMessageProcessor;
import java.net.URI;

public class Router extends AbstractInterceptingMessageProcessor implements Initialisable

{
    @Inject
    private ApikitRegistry registry;

    private String configRef;

    private String name;

    @Override
    public void initialise() throws InitialisationException
    {
        URI uri = MessageSourceUtils.getUriFromFlow((Flow) flowConstruct);
        registry.setApiSource(configRef, uri.toString().replace("*",""));
    }

    public Event process(Event event) throws MuleException {
        Configuration config = registry.getConfiguration(getConfigRef());
        event = EventHelper.addVariable(event, config.getOutboundHeadersMapName(), new HashMap<>());
//        event = EventHelper.addVariable(event, config.getHttpStatusVarName(), "201");

        HttpRequestAttributes attributes = ((HttpRequestAttributes)event.getMessage().getAttributes().getValue());

        String path = UrlUtils.getRelativePath(attributes);
        path = path.isEmpty() ? "/" : path;

        //Get uriPattern, uriResolver, and the resolvedVariables
        URIPattern uriPattern;
        URIResolver uriResolver;
        try {
            uriPattern = config.getUriPatternCache().get(path);
            uriResolver = config.getUriResolverCache().get(path);
        } catch (ExecutionException e) {
            throw new DefaultMuleException(e);
        }
        ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);
        IResource resource = getResource(config, attributes.getMethod().toLowerCase(), uriPattern);
        if (!config.isDisableValidations())
        {
            event = validateRequest(event, config, resource, attributes, resolvedVariables);
        }
        String contentType = AttributesHelper.getHeaderIgnoreCase((HttpRequestAttributes) event.getMessage().getAttributes().getValue(), HeaderNames.CONTENT_TYPE);
        Flow flow = config.getFlowFinder().getFlow(resource,attributes.getMethod().toLowerCase(), contentType);
        String successStatusCode = config.getRamlHandler().getSuccessStatusCode(resource.getAction(attributes.getMethod().toLowerCase()));
        event = EventHelper.addVariable(event, config.getHttpStatusVarName(), successStatusCode);

        event = flow.process(event);

        return event;
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

    public Event validateRequest(Event event, ValidationConfig config, IResource resource, HttpRequestAttributes attributes, ResolvedVariables resolvedVariables) throws DefaultMuleException, MuleRestException {

        String charset = null;
        try {
            charset = getEncoding(event.getMessage(), event.getMessage().getPayload().getValue(), logger);
        } catch (IOException e) {
            throw ApikitErrorTypes.throwErrorTypeNew(new BadRequestException("Error processing request: " + e.getMessage()));
        }

        ValidRequest validRequest = RequestValidator.validate(config, resource, attributes, resolvedVariables, event.getMessage().getPayload().getValue(), charset);

        return EventHelper.regenerateEvent(event, validRequest);
    }

    private IResource getResource(Configuration configuration, String method, URIPattern uriPattern) throws TypedException
    {
        IResource resource = configuration.getFlowFinder().getResource(uriPattern);
        if (resource.getAction(method) == null) {
            throw ApikitErrorTypes.throwErrorTypeNew(new MethodNotAllowedException(resource.getUri() + " : " + method));
        }
        return resource;
    }

}
