/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.CharsetUtils.getEncoding;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.flatMap;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.fromFuture;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

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
import org.mule.module.apikit.input.stream.RewindableInputStream;
import org.mule.module.apikit.spi.EventProcessor;
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
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.StringMessageUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;

public class Router extends AbstractComponent implements Processor, Initialisable, EventProcessor

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

    LOGGER.info(StringMessageUtils.getBoilerPlate("APIKit Started with Parser: " + configuration.getParser().name()));
  }

  @Override
  public CoreEvent process(final CoreEvent event) throws MuleException {
    return processToApply(event, this);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return flatMap(publisher, this::processWithExtension, this);
  }

  private Publisher<CoreEvent> processWithExtension(CoreEvent event) {
    try {
      final CompletableFuture<Event> resultEvent;
      if (configuration.isExtensionEnabled()) {
        resultEvent = configuration.getExtension().process(event, this, this.configuration.getRaml());
      } else {
        resultEvent = processEvent(event);
      }

      return fromFuture(resultEvent).cast(CoreEvent.class).onErrorMap(this::buildMuleException);
    } catch (MuleRestException e) {
      return error(ApikitErrorTypes.throwErrorType(e));
    } catch (MuleException e) {
      return error(e);
    }
  }

  public CompletableFuture<Event> processEvent(CoreEvent event) throws MuleRestException {
    Configuration config = registry.getConfiguration(getConfiguration().getName());
    CoreEvent.Builder eventBuilder = CoreEvent.builder(event);
    eventBuilder.addVariable(config.getOutboundHeadersMapName(), new HashMap<>());

    HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());

    return doRoute(event, config, eventBuilder, attributes);
  }

  private Throwable buildMuleException(Throwable e) {
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
      throws MuleRestException {
    String path = UrlUtils.getRelativePath(attributes.getListenerPath(), attributes.getRequestPath());
    path = path.isEmpty() ? "/" : path;

    //Get uriPattern, uriResolver, and the resolvedVariables
    URIPattern uriPattern = findInCache(path, config.getUriPatternCache());
    URIResolver uriResolver = findInCache(path, config.getUriResolverCache());
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

  private IResource getResource(Configuration configuration, String method, URIPattern uriPattern) throws TypedException {
    IResource resource = configuration.getFlowFinder().getResource(uriPattern);
    if (resource.getAction(method) == null) {
      throw ApikitErrorTypes.throwErrorType(new MethodNotAllowedException(resource
          .getResolvedUri(configuration.getRamlHandler().getApi().getVersion()) + " : " + method));
    }
    return resource;
  }

}
