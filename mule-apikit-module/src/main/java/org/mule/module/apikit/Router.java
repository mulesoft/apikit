/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static reactor.core.publisher.Flux.from;

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
import org.mule.module.apikit.exception.MethodNotAllowedException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.execution.ComponentExecutionException;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.DefaultMuleException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    final Optional<URI> url = locator.find(Location.builder().globalName(name).addSourcePart().build())
        .map(MessageSourceUtils::getUriFromFlow);

    if (!url.isPresent()) {
      LOGGER
          .error("There was an error retrieving api source. Console will work only if the keepRamlBaseUri property is set to true.");
      return;
    }
    registry.setApiSource(configuration.getName(), url.get().toString().replace("*", ""));
  }

  @Override
  public CoreEvent process(final CoreEvent event) throws MuleException {
    try {
      Configuration config = registry.getConfiguration(getConfiguration().getName());
      CoreEvent.Builder eventBuilder = CoreEvent.builder(event);
      eventBuilder.addVariable(config.getOutboundHeadersMapName(), new HashMap<>());

      HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

      return (CoreEvent) doRoute(event, config, eventBuilder, attributes).get();
    } catch (MuleRestException e) {
      throw ApikitErrorTypes.throwErrorType(e);
    } catch (ComponentExecutionException e) {
      throw new TypedException(e.getCause(), e.getEvent().getError().get().getErrorType());
    } catch (MuleException | MuleRuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new DefaultMuleException(e);
    }
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return from(publisher).flatMap(event -> {
      try {
        Configuration config = registry.getConfiguration(getConfiguration().getName());
        CoreEvent.Builder eventBuilder = CoreEvent.builder(event);
        eventBuilder.addVariable(config.getOutboundHeadersMapName(), new HashMap<>());

        HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

        final CompletableFuture<Event> resultEvent = doRoute(event, config, eventBuilder, attributes);

        return Mono.fromFuture(resultEvent).cast(CoreEvent.class).onErrorMap(this::buildMessagingException);
      } catch (Exception e) {
        return Flux.error(buildMessagingException(e));
      }
    });
  }

  private Throwable buildMessagingException(Throwable e) {
    if (e instanceof MuleRestException) {
      return ApikitErrorTypes.throwErrorType((MuleRestException) e);
    }

    if (e instanceof ComponentExecutionException) {
      return new TypedException(e.getCause(),
                                ((ComponentExecutionException) e).getEvent().getError().get().getErrorType());
    }

    if (e instanceof MuleException || e instanceof MuleRuntimeException) {
      return e;
    }

    return new DefaultMuleException(e);
  }

  private CompletableFuture<Event> doRoute(CoreEvent event, Configuration config, CoreEvent.Builder eventBuilder,
                                           HttpRequestAttributes attributes)
      throws ExecutionException, DefaultMuleException, MuleRestException {
    String path = UrlUtils.getRelativePath(attributes.getListenerPath(), attributes.getRequestPath());
    path = path.isEmpty() ? "/" : path;

    //Get uriPattern, uriResolver, and the resolvedVariables
    URIPattern uriPattern;
    try {
      uriPattern = config.getUriPatternCache().get(path);
    } catch (Exception e) {
      throw ApikitErrorTypes.throwErrorType(new NotFoundException(path));
    }

    URIResolver uriResolver = config.getUriResolverCache().get(path);

    ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);
    IResource resource = getResource(config, attributes.getMethod().toLowerCase(), uriPattern);
    if (!config.isDisableValidations()) {
      eventBuilder = validateRequest(event, eventBuilder, config, resource, attributes, resolvedVariables);
    }
    String contentType = AttributesHelper.getMediaType(attributes);
    Flow flow = config.getFlowFinder().getFlow(resource, attributes.getMethod().toLowerCase(), contentType);
    String successStatusCode =
        config.getRamlHandler().getSuccessStatusCode(resource.getAction(attributes.getMethod().toLowerCase()));
    eventBuilder.addVariable(config.getHttpStatusVarName(), successStatusCode);
    return flow.execute(eventBuilder.build());
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
      throws DefaultMuleException, MuleRestException {

    String charset = null;
    try {
      charset = getEncoding(event.getMessage(), event.getMessage().getPayload().getValue(), LOGGER);
    } catch (IOException e) {
      throw ApikitErrorTypes.throwErrorType(new BadRequestException("Error processing request: " + e.getMessage()));
    }

    ValidRequest validRequest =
        RequestValidator.validate(config, resource, attributes, resolvedVariables, event.getMessage().getPayload(), charset);

    return EventHelper.regenerateEvent(event.getMessage(), eventBuilder, validRequest);
  }

  private IResource getResource(Configuration configuration, String method, URIPattern uriPattern) throws TypedException {
    IResource resource = configuration.getFlowFinder().getResource(uriPattern);
    if (resource.getAction(method) == null) {
      throw ApikitErrorTypes.throwErrorType(new MethodNotAllowedException(resource.getUri() + " : " + method));
    }
    return resource;
  }

}
