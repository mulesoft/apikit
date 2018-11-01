/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static org.mule.apikit.common.FlowName.FLOW_NAME_SEPARATOR;

public class ApiCoordinateFactory {

  private Set<String> configNames;

  public ApiCoordinateFactory(final Set<String> configNames) {
    this.configNames = configNames;
  }

  public Optional<ApiCoordinate> fromFlowName(final String flowName) {

    final String[] parts = org.mule.apikit.common.FlowName.decode(flowName).split(FLOW_NAME_SEPARATOR);

    if (parts.length < 2 || parts.length > 4)
      return empty();

    final ApiCoordinateBuilder builder = ApiCoordinateBuilder.create(flowName, parts, configNames);

    return builder.build();

  }

  public ApiCoordinate createFromFlowMapping(final FlowMapping mapping) {

    final String flowName = mapping.getFlowRef();
    final String configName = mapping.getConfigName();
    final String action = mapping.getAction();
    final String resource = mapping.getResource();
    final String contentType = mapping.getContentType();
    return new ApiCoordinate(flowName, action, resource, contentType, configName);
  }

  private static class ApiCoordinateBuilder {

    final private String flowName;
    final private String methodName;
    final private String resourceName;
    private String mediaType = null;
    private String configName = null;
    private Set<String> configNames = Collections.emptySet();

    private ApiCoordinateBuilder(final String flowName, final String methodName, final String resourceName) {
      this.flowName = flowName;
      this.methodName = methodName;
      this.resourceName = resourceName;
    }

    static ApiCoordinateBuilder create(final String flowName, final String[] parts, final Set<String> configNames) {

      final ApiCoordinateBuilder builder = new ApiCoordinateBuilder(flowName, parts[0], parts[1]);

      if (parts.length == 3) {
        if (configNames.contains(parts[2])) {
          builder.withConfigName(parts[2]);
        } else {
          builder.withMediaType(parts[2]);
        }
      } else if (parts.length == 4) {
        builder.withMediaType(parts[2]).withConfigName(parts[3]);
      }

      builder.withConfigNames(configNames);

      return builder;
    }

    ApiCoordinateBuilder withMediaType(final String value) {
      this.mediaType = value;
      return this;
    }

    ApiCoordinateBuilder withConfigName(final String value) {
      this.configName = value;
      return this;
    }

    ApiCoordinateBuilder withConfigNames(final Set<String> configNames) {
      this.configNames = configNames;
      return this;
    }

    private Optional<ApiCoordinate> build() {
      final ApiCoordinate coord = new ApiCoordinate(flowName, methodName, resourceName, mediaType, configName);

      if ((coord.getConfigName() != null && !configNames.contains(coord.getConfigName()))
          || (configNames.size() > 1 && coord.getConfigName() == null)) {
        return empty();
      }
      return Optional.of(coord);
    }
  }
}
