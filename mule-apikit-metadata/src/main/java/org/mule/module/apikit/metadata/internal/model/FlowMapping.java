/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

public class FlowMapping {

  private String resource;
  private String action;
  private String contentType;
  private String flowRef;
  private String configName;

  public FlowMapping(String configName, String resource, String action, String contentType, String flowRef) {
    this.configName = configName;
    this.resource = resource;
    this.action = action;
    this.contentType = contentType;
    this.flowRef = flowRef;
  }

  public String getResource() {
    return resource;
  }

  public String getAction() {
    return action;
  }

  public String getContentType() {
    return contentType;
  }

  public String getFlowRef() {
    return flowRef;
  }

  public String getConfigName() {
    return configName;
  }
}
