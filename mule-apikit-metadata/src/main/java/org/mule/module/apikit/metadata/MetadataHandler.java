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
    final RamlCoordinate coordinate = modelWrapper.getRamlCoordinatesForFlow(flowName);

    if (coordinate == null) {
      notifier.error(format("There is no metadata for flow '%s'", flowName));
      return empty();
    }

    // If there exists metadata for the flow, we get the Api
    final ApikitConfig config = modelWrapper.getApikitConfigWithName(coordinate.getConfigName());
    return config.getApi()
        .map(api -> api.getActionForCoordinate(coordinate))
        .flatMap(MetadataSource::getMetadata);
  }
}
