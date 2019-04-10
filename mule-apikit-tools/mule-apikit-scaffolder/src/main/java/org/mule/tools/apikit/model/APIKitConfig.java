/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

public class APIKitConfig {

  public static final String ELEMENT_NAME = "config";
  public static final String NAME_ATTRIBUTE = "name";
  public static final String API_ATTRIBUTE = "api";
  public static final String RAML_ATTRIBUTE = "raml";
  public static final String EXTENSION_ENABLED_ATTRIBUTE = "extensionEnabled";
  public static final String DEFAULT_CONFIG_NAME = "config";
  public static final String OUTBOUND_HEADERS_MAP_ATTRIBUTE = "outboundHeadersMapName";
  public static final String DEFAULT_OUTBOUND_HEADERS_MAP_NAME = "outboundHeaders";
  public static final String HTTP_STATUS_VAR_ATTRIBUTE = "httpStatusVarName";
  public static final String DEFAULT_HTTP_STATUS_NAME = "httpStatus";

  private String name;
  private String api;
  private String raml;
  private Boolean extensionEnabled = null;
  private String outboundHeadersMapName = DEFAULT_OUTBOUND_HEADERS_MAP_NAME;
  private String httpStatusVarName = DEFAULT_HTTP_STATUS_NAME;

  public APIKitConfig(
                      final String name,
                      final String api,
                      final Boolean extensionEnabled,
                      final String outboundHeadersMapName,
                      final String httpStatusVarName) {
    this.name = name;
    this.api = api;
    this.extensionEnabled = extensionEnabled;
    this.outboundHeadersMapName = outboundHeadersMapName;
    this.httpStatusVarName = httpStatusVarName;
  }

  public APIKitConfig() {}

  public String getName() {
    return name;
  }

  public String getApi() {
    return api;
  }

  public String getRaml() {
    return raml;
  }

  public Boolean isExtensionEnabled() {
    return extensionEnabled;
  }

  public void setExtensionEnabled(Boolean enabled) {
    this.extensionEnabled = enabled;
  }

  public void setName(String name) {
    this.name = name;
    if (name == null) {
      this.name = APIKitConfig.DEFAULT_CONFIG_NAME;
    }
  }

  public void setExtensionEnabled(boolean extensionEnabled) {
    this.extensionEnabled = extensionEnabled;
  }

  public void setOutboundHeadersMapName(String outboundHeadersMapName) {
    this.outboundHeadersMapName = outboundHeadersMapName;
  }

  public void setHttpStatusVarName(String httpStatusVarName) {
    this.httpStatusVarName = httpStatusVarName;
  }

  public String getOutboundHeadersMapName() {
    return outboundHeadersMapName;
  }

  public String getHttpStatusVarName() {
    return httpStatusVarName;
  }

  public void setApi(String api) {
    this.api = api;
  }

  public void setRaml(String raml) {
    this.raml = raml;
  }
}
