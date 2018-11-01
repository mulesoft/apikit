/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.api.Metadata;
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.config.internal.model.ApplicationModel;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.empty;

public class MetadataModel implements Metadata {

  private final ApplicationModelWrapper applicationModel;
  private final Map<String, String> httpStatus; // [{config -> http status var name}]
  private final Map<String, String> outboundHeaders; // [{config -> output header map name}]

  public MetadataModel(final ApplicationModel model, final ResourceLoader loader, final Notifier notifier) {
    this.applicationModel = new ApplicationModelWrapper(model, loader, notifier);
    this.httpStatus = loadHttpStatusVars(applicationModel);
    this.outboundHeaders = loadOutboundHeaders(applicationModel);
  }

  // protected abstract MetadataResolverFactory createMetadataResolverFactory(final ResourceLoader loader, final Notifier notifier);

  /**
   * Gets the metadata for a Flow
   * @param flowName Name of the flow
   * @return The Metadata
   */
  public Optional<FunctionType> getMetadataForFlow(final String flowName) {
    // Getting the RAML Coordinate for the specified flowName
    final Optional<ApiCoordinate> coordinate = applicationModel.getApiCoordinate(flowName);

    if (!coordinate.isPresent()) {
      return empty();
    }

    final Optional<ApikitConfig> config = applicationModel.getConfig(coordinate.get().getConfigName());

    if (!config.isPresent())
      return empty();

    final String httpStatusVar = httpStatus.get(config.get().getName());
    final String outboundHeadersVar = outboundHeaders.get(config.get().getName());

    if (isNullOrEmpty(httpStatusVar) || isNullOrEmpty(outboundHeadersVar)) {
      return empty();
    }

    // If there exists metadata for the flow, we get the Api
    return config
        .flatMap(ApikitConfig::getMetadataResolver)
        .flatMap(resolver -> resolver.getMetadataSource(coordinate.get(), httpStatusVar, outboundHeadersVar))
        .flatMap(MetadataSource::getMetadata);
  }

  private static Map<String, String> loadOutboundHeaders(final ApplicationModelWrapper modelWrapper) {
    final Map<String, String> outboundHeaders = new HashMap<>();

    modelWrapper.getConfigurations().forEach(c -> outboundHeaders.put(c.getName(), c.getOutputHeadersVarName()));

    return outboundHeaders;
  }

  private static Map<String, String> loadHttpStatusVars(final ApplicationModelWrapper modelWrapper) {
    final Map<String, String> httpStatusVars = new HashMap<>();

    modelWrapper.getConfigurations().forEach(c -> httpStatusVars.put(c.getName(), c.getHttpStatusVarName()));

    return httpStatusVars;
  }
}
