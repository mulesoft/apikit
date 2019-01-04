/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.File;

public class API {

  public static final String DEFAULT_HOST = "0.0.0.0";
  public static final int DEFAULT_PORT = 8081;
  public static final String DEFAULT_BASE_URI = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/api";
  public static final String DEFAULT_BASE_PATH = "/";
  public static final String DEFAULT_PROTOCOL = "HTTP";
  public static final String DEFAULT_CONSOLE_PATH = "/console/*";
  public static final String DEFAULT_CONSOLE_PATH_INBOUND = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + "/console";

  private APIKitConfig config;
  private HttpListener4xConfig httpListenerConfig;
  private String path;

  private String baseUri;
  private File xmlFile;
  private String apiFilPath;
  private String id;

  public API(String id, String apiFilePath, File xmlFile, String baseUri, String path) {
    this.path = path;
    this.apiFilPath = apiFilePath;
    this.xmlFile = xmlFile;
    this.baseUri = baseUri;
    this.id = id;
  }


  public API(String id, String apiFileName, File xmlFile, String baseUri, String path, APIKitConfig config) {
    this(id, apiFileName, xmlFile, baseUri, path);
    this.config = config;
  }

  public File getXmlFile() {
    return xmlFile;
  }

  public void setXmlFile(File xmlFile) {
    this.xmlFile = xmlFile;
  }

  public File getXmlFile(File rootDirectory) {
    // Case we need to create the file
    if (xmlFile == null) {
      xmlFile = new File(rootDirectory, id + ".xml");
    }
    return xmlFile;
  }

  public String getApiFilePath() {
    return apiFilPath;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public HttpListener4xConfig getHttpListenerConfig() {
    return httpListenerConfig;
  }

  public APIKitConfig getConfig() {
    return config;
  }

  public void setConfig(APIKitConfig config) {
    this.config = config;
  }

  public void setHttpListenerConfig(HttpListener4xConfig httpListenerConfig) {
    this.httpListenerConfig = httpListenerConfig;
  }

  public void setDefaultAPIKitConfig() {
    config = new APIKitConfig();
    config.setApi(apiFilPath);
    config.setName(id + "-" + APIKitConfig.DEFAULT_CONFIG_NAME);
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    API api = (API) o;

    if (!apiFilPath.equals(api.apiFilPath) || !id.equals(api.id))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return apiFilPath.hashCode();
  }

  public String getId() {
    return id;
  }

}
