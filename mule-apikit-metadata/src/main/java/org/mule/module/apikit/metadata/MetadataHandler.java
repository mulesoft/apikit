/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.interfaces.MetadataSource;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.model.ApikitConfig;
import org.mule.module.apikit.metadata.model.RamlCoordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.empty;

public class MetadataHandler {

  private ApplicationModelWrapper modelWrapper;
  private Notifier notifier;
  private Map<String, String> httpStatus; // [{config -> http status var name}]
  private Map<String, String> outboundHeaders; // [{config -> output header map name}]

  public MetadataHandler(ApplicationModelWrapper modelWrapper, Notifier notifier) {
    this.modelWrapper = modelWrapper;
    this.notifier = notifier;
    this.httpStatus = loadHttpStatusVars(modelWrapper);
    this.outboundHeaders = loadOutboundHeaders(modelWrapper);
  }

  private static Map<String, String> loadOutboundHeaders(ApplicationModelWrapper modelWrapper) {
    final Map<String, String> outboundHeaders = new HashMap<>();

    modelWrapper.getConfigurations().forEach(c -> outboundHeaders.put(c.getName(), c.getOutputHeadersVarName()));

    return outboundHeaders;
  }

  private static Map<String, String> loadHttpStatusVars(ApplicationModelWrapper modelWrapper) {
    final Map<String, String> httpStatusVars = new HashMap<>();

    modelWrapper.getConfigurations().forEach(c -> httpStatusVars.put(c.getName(), c.getHttpStatusVarName()));

    return httpStatusVars;
  }

  public Optional<FunctionType> getMetadataForFlow(String flowName) {
    // Getting the RAML Coordinate for the specified flowName
    final Optional<RamlCoordinate> coordinate = modelWrapper.getRamlCoordinatesForFlow(flowName);

    if (!coordinate.isPresent()) {
      return empty();
    }

    final Optional<ApikitConfig> config = modelWrapper.getConfig(coordinate.get().getConfigName());

    if (!config.isPresent()) {
      return empty();
    }

    final String httpStatusVar = httpStatus.get(config.get().getName());
    final String outboundHeadersVar = outboundHeaders.get(config.get().getName());

    if (isNullOrEmpty(httpStatusVar) || isNullOrEmpty(outboundHeadersVar)) {
      return empty();
    }

    // If there exists metadata for the flow, we get the Api
    return config
        .flatMap(ApikitConfig::getApi)
        .flatMap(api -> api.getActionForFlow(coordinate.get(), httpStatusVar, outboundHeadersVar))
        .flatMap(MetadataSource::getMetadata);
  }
}
