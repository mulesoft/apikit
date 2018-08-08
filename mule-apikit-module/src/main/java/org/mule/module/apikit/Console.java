/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.console.ConsoleResources;
import org.mule.module.apikit.api.console.Resource;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.helpers.EventWrapper;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.StringMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Optional;

public class Console extends AbstractComponent implements Processor, Initialisable {

  private final ApikitRegistry registry;
  private final ConfigurationComponentLocator locator;

  private Configuration configuration;
  private String name;
  protected static final Logger logger = LoggerFactory.getLogger(Console.class);

  private static final String CONSOLE_URL_FILE = "consoleurl";

  @Inject
  private MuleContext muleContext;

  @Inject
  public Console(ApikitRegistry registry, ConfigurationComponentLocator locator) {
    this.registry = registry;
    this.locator = locator;
  }

  @Override
  public void initialise() throws InitialisationException {
    final String name = getLocation().getRootContainerName();

    final Optional<URI> url = locator.find(Location.builder().globalName(name).addSourcePart().build())
        .map(MessageSourceUtils::getUriFromFlow);

    if (url.isPresent()) {
      URI uri = url.get();
      String consoleUrl = uri.toString().replace("*", "");
      String consoleUrlFixed = UrlUtils.getBaseUriReplacement(consoleUrl);
      logger.info(StringMessageUtils.getBoilerPlate("APIKit Console URL: " + consoleUrlFixed));
      publishConsoleUrls(consoleUrlFixed);
    } else {
      logger.error("There was an error retrieving console source.");
    }
  }

  @Override
  public CoreEvent process(CoreEvent event) throws MuleException {
    final Configuration config = getConfiguration();

    EventWrapper eventWrapper = new EventWrapper(event, config.getOutboundHeadersMapName(), config.getHttpStatusVarName());

    HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(event);
    String listenerPath = attributes.getListenerPath();
    String requestPath = attributes.getRequestPath();
    String acceptHeader = AttributesHelper.getHeaderIgnoreCase(attributes, "Accept");
    String queryString = attributes.getQueryString();
    String method = attributes.getMethod();

    ConsoleResources consoleResources =
        new ConsoleResources(config, listenerPath,
                             requestPath, queryString, method, acceptHeader);

    // Listener path MUST end with /*
    consoleResources.isValidPath(attributes.getListenerPath());

    String consoleBasePath = UrlUtils.getBasePath(listenerPath, requestPath);
    String resourceRelativePath = UrlUtils.getRelativePath(listenerPath, requestPath);

    // If the request was made to, for example, /console, we must redirect the client to /console/
    if (!consoleBasePath.endsWith("/")) {
      eventWrapper.doClientRedirect();
      return eventWrapper.build();
    }

    Resource resource = consoleResources.getConsoleResource(resourceRelativePath);

    eventWrapper.setPayload(resource.getContent(), resource.getMediaType());
    eventWrapper.addOutboundProperties(resource.getHeaders());

    return eventWrapper.build();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private void publishConsoleUrls(final String consoleUrl) {
    FileWriter writer = null;

    try {
      final String parentDirectory = muleContext.getConfiguration().getWorkingDirectory();
      File urlFile = new File(parentDirectory, CONSOLE_URL_FILE);
      if (!urlFile.exists()) {
        urlFile.createNewFile();
      }
      writer = new FileWriter(urlFile, true);
      writer.write(consoleUrl + "\n");
      writer.flush();
    } catch (Exception e) {
      logger.error("cannot publish console url for studio", e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }
}
