/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.google.common.cache.LoadingCache;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.validation.RequestValidator;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.module.apikit.api.FlowUtils.getSourceLocation;
import static org.mule.runtime.core.api.event.CoreEvent.builder;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.flatMap;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.processWithChildContext;
import static reactor.core.publisher.Flux.from;
import static reactor.core.publisher.Mono.error;

public class Router extends AbstractComponent implements Processor, Initialisable

{

  private final ApikitRegistry registry;

  private final ConfigurationComponentLocator locator;

  private Configuration configuration;

  private String name;

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  @Inject
  public Router(ApikitRegistry registry, ConfigurationComponentLocator locator) {
    this.registry = registry;
    this.locator = locator;
  }

  @Override
  public void initialise() throws InitialisationException {
    final String name = getLocation().getRootContainerName();
    final Optional<URI> url = getSourceLocation(locator, name);

    if (!url.isPresent()) {
      LOGGER
          .error("There was an error retrieving api source. Console will work only if the keepRamlBaseUri property is set to true.");
      return;
    }
    registry.setApiSource(configuration.getName(), url.get().toString().replace("*", ""));
  }

  @Override
  public CoreEvent process(final CoreEvent event) throws MuleException {
    return processToApply(event, this);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return flatMap(publisher, event -> {
      try {
        Configuration config = registry.getConfiguration(getConfiguration().getName());
        CoreEvent.Builder eventBuilder = builder(event);
        eventBuilder.addVariable(config.getOutboundHeadersMapName(), new HashMap<>());

        HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

        return doRoute(event, config, eventBuilder, attributes);
      } catch (MuleRestException e) {
        return error(ApikitErrorTypes.throwErrorType(e));
      }
    }, this);
  }

  private Publisher<CoreEvent> doRoute(CoreEvent event, Configuration config, CoreEvent.Builder eventBuilder,
                                       HttpRequestAttributes attributes)
      throws MuleRestException {
    String path = UrlUtils.getRelativePath(attributes.getListenerPath(), attributes.getRawRequestPath());
    path = path.isEmpty() ? "/" : path;

    //Get uriPattern, uriResolver, and the resolvedVariables
    URIPattern uriPattern = findInCache(path, config.getUriPatternCache());
    URIResolver uriResolver = findInCache(path, config.getUriResolverCache());
    ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);

    IResource resource = config.getFlowFinder().getResource(uriPattern);
    eventBuilder = validateRequest(event, eventBuilder, config, resource, attributes, resolvedVariables);
    String contentType = AttributesHelper.getMediaType(attributes);
    Flow flow = config.getFlowFinder().getFlow(resource, attributes.getMethod().toLowerCase(), contentType);

    final Publisher<CoreEvent> flowResult =
        processWithChildContext(eventBuilder.build(), flow, ofNullable(getLocation()), flow.getExceptionListener());

    return from(flowResult)
        .map(result -> {
          if (result.getVariables().get(config.getHttpStatusVarName()) == null) {
            // If status code is missing, a default one is added
            final String successStatusCode =
                config.getRamlHandler().getSuccessStatusCode(resource.getAction(attributes.getMethod().toLowerCase()));
            return builder(result).addVariable(config.getHttpStatusVarName(), successStatusCode).build();
          }
          return result;
        });
  }

  private <T> T findInCache(String key, LoadingCache<String, T> cache) {
    try {
      return cache.get(key);
    } catch (Exception e) {
      throw ApikitErrorTypes.throwErrorType(new NotFoundException(key));
    }
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration config) {
    this.configuration = config;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CoreEvent.Builder validateRequest(CoreEvent event, CoreEvent.Builder eventBuilder, ValidationConfig config,
                                           IResource resource, HttpRequestAttributes attributes,
                                           ResolvedVariables resolvedVariables)
      throws MuleRestException {

    TypedValue payload = event.getMessage().getPayload();

    final String charset;
    try {
      final Object payloadValue = payload.getValue();
      if (payloadValue instanceof InputStream) {
        final RewindableInputStream rewindable = new RewindableInputStream((InputStream) payloadValue);
        charset = getEncoding(event.getMessage(), rewindable, LOGGER);
        rewindable.rewind();
        payload = new TypedValue<>(rewindable, payload.getDataType());
      } else {
        charset = getEncoding(event.getMessage(), payloadValue, LOGGER);
      }
    } catch (IOException e) {
      throw ApikitErrorTypes.throwErrorType(new BadRequestException("Error processing request: " + e.getMessage()));
    }

    final ValidRequest validRequest =
        RequestValidator.validate(config, resource, attributes, resolvedVariables, payload, charset);

    return EventHelper.regenerateEvent(event.getMessage(), eventBuilder, validRequest);
  }

}
