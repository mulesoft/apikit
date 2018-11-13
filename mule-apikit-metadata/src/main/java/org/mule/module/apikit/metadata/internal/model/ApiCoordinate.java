/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import javax.annotation.Nullable;

/**
 * A RAML coordinate. It is composed by a method, a resource, a media type (optional), and a APIkit config name (optional).
 */
public class ApiCoordinate {

  final private String flowName;
  final private String method;
  final private String resource;
  final private String mediaType;
  final private String configName;

  public ApiCoordinate(String flowName, String method, String resource, String mediaType, String configName) {
    this.flowName = flowName;
    this.resource = resource;
    this.method = method;
    this.mediaType = mediaType;
    this.configName = configName;
  }

  @Nullable
  public String getConfigName() {
    return configName;
  }

  public String getResource() {
    return resource;
  }

  @Nullable
  public String getMediaType() {
    return mediaType;
  }

  public String getMethod() {
    return method;
  }

  public String getFlowName() {
    return flowName;
  }
}
