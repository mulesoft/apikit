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

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;

public class MetadataHandler {

  private ApplicationModelWrapper modelWrapper;
  private Notifier notifier;

  public MetadataHandler(ApplicationModelWrapper modelWrapper, Notifier notifier) {
    this.modelWrapper = modelWrapper;
    this.notifier = notifier;
  }

  public Optional<FunctionType> getMetadataForFlow(String flowName) {
    // Getting the RAML Coordinate for the specified flowName
    final Optional<RamlCoordinate> coordinate = modelWrapper.getRamlCoordinatesForFlow(flowName);

    if (!coordinate.isPresent()) {
      return empty();
    }

    // If there exists metadata for the flow, we get the Api
    final Optional<ApikitConfig> config = modelWrapper.getConfig(coordinate.get().getConfigName());
    return config.flatMap(ApikitConfig::getApi)
        .map(api -> api.getActionForCoordinate(coordinate.get()))
        .flatMap(MetadataSource::getMetadata);
  }
}
