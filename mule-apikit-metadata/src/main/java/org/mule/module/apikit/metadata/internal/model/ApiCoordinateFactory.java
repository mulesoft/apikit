/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import org.mule.apikit.common.FlowName;
import org.mule.module.apikit.metadata.internal.model.FlowMapping;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static org.mule.apikit.common.FlowName.FLOW_NAME_SEPARATOR;

public class ApiCoordinateFactory {

  private Set<String> apiConfigNames;

  public ApiCoordinateFactory(Set<String> apiConfigNames) {
    this.apiConfigNames = apiConfigNames;
  }

  public Optional<ApiCoordinate> createFromFlowName(String flowName) {

    final String[] parts = FlowName.decode(flowName).split(FLOW_NAME_SEPARATOR);

    if (parts.length < 2 || parts.length > 4) {
      return empty();
    }

    final Builder builder = Builder.create(flowName, parts[0], parts[1]);

    if (parts.length == 3) {
      if (apiConfigNames.contains(parts[2])) {
        builder.configName(parts[2]);
      } else {
        builder.mediaType(parts[2]);
      }
    } else if (parts.length == 4) {
      builder.mediaType(parts[2]).configName(parts[3]);
    }

    final ApiCoordinate coord = builder.build();

    if (coord.getConfigName() != null && !apiConfigNames.contains(coord.getConfigName())) {
      return empty();
    }

    if (apiConfigNames.size() > 1 && coord.getConfigName() == null) {
      return empty();
    }

    return Optional.of(coord);
  }


  public ApiCoordinate createFromFlowMapping(FlowMapping mapping) {

    final String flowName = mapping.getFlowRef();
    final String configName = mapping.getConfigName();
    final String action = mapping.getAction();
    final String resource = mapping.getResource();
    final String contentType = mapping.getContentType();
    return new ApiCoordinate(flowName, action, resource, contentType, configName);
  }

  private static class Builder {

    final private String flowName;
    final private String methodName;
    final private String resourceName;
    private String mediaType = null;
    private String configName = null;

    private Builder(String flowName, String methodName, String resourceName) {
      this.flowName = flowName;
      this.methodName = methodName;
      this.resourceName = resourceName;
    }

    static Builder create(String key, String methodName, String resourceName) {
      return new Builder(key, methodName, resourceName);
    }

    Builder mediaType(String value) {
      this.mediaType = value;
      return this;
    }

    Builder configName(String value) {
      this.configName = value;
      return this;
    }

    private ApiCoordinate build() {
      return new ApiCoordinate(flowName, methodName, resourceName, mediaType, configName);
    }
  }
}
