/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.FileUtils;
import org.mule.apikit.common.APISyncUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;
import static java.util.stream.Collectors.toList;
import static org.mule.tools.apikit.model.APIKitConfig.DEFAULT_CONFIG_NAME;

public class APIFactory {

  private static final String RESOURCE_API_FOLDER =
      "src" + separator + "main" + separator + "resources" + separator + "api" + separator;

  private Map<String, API> apis = new HashMap<>();
  private Map<String, HttpListener4xConfig> domainHttpListenerConfigs = new HashMap<>();

  public APIFactory(Map<String, HttpListener4xConfig> domainHttpListenerConfigs) {
    this.domainHttpListenerConfigs.putAll(domainHttpListenerConfigs);
  }

  public APIFactory() {}

  public API createAPIBindingInboundEndpoint(String ramlFileName, File xmlFile, String baseUri, String path,
                                             APIKitConfig config) {
    return createAPIBinding(ramlFileName, xmlFile, baseUri, path, config, null);
  }

  public API createAPIBinding(String ramlFilePath, File xmlFile, String baseUri, String path, APIKitConfig config,
                              HttpListener4xConfig httpListenerConfig) {

    Validate.notNull(ramlFilePath);
    final String relativePath = getRelativePath(ramlFilePath);
    if (apis.containsKey(relativePath)) {
      API api = apis.get(relativePath);
      if (api.getXmlFile() == null && xmlFile != null) {
        api.setXmlFile(xmlFile);
      }
      return api;
    }
    final String id = buildApiId(relativePath);
    API api = new API(id, relativePath, xmlFile, baseUri, path, config);
    if (httpListenerConfig == null) {
      if (domainHttpListenerConfigs.size() > 0) {
        api.setHttpListenerConfig(getFirstLC());
      } else {
        api.setDefaultHttpListenerConfig();
      }
    } else {
      api.setHttpListenerConfig(httpListenerConfig);
    }
    api.setConfig(config);
    apis.put(relativePath, api);
    return api;
  }

  private String buildApiId(String ramlFilePath) {
    final String apiId;

    if (APISyncUtils.isSyncProtocol(ramlFilePath))
      apiId = FilenameUtils.removeExtension(APISyncUtils.getFileName(ramlFilePath));
    else
      apiId = FilenameUtils.removeExtension(FileUtils.basename(ramlFilePath)).trim();

    final List<String> apiIds = apis.values().stream().map(API::getId).collect(toList());

    final List<String> configNames = apis.values().stream()
        .filter(a -> a.getConfig() != null)
        .map(a -> a.getConfig().getName()).collect(toList());

    final List<String> httpConfigNames = apis.values().stream()
        .filter(a -> a.getHttpListenerConfig() != null)
        .map(a -> a.getHttpListenerConfig().getName()).collect(toList());

    int count = 0;
    String id;
    do {
      count++;
      id = (count > 1 ? apiId + "-" + count : apiId);
    } while (apiIds.contains(id) || configNames.contains(id + "-" + DEFAULT_CONFIG_NAME)
        || httpConfigNames.contains(id + "-" + HttpListener4xConfig.DEFAULT_CONFIG_NAME));

    return id;
  }

  private String getRelativePath(String path) {
    if (!APISyncUtils.isSyncProtocol(path)
        && !(path.startsWith("http://") || path.startsWith("https://"))
        && path.contains(RESOURCE_API_FOLDER))
      return path.substring(path.lastIndexOf(RESOURCE_API_FOLDER) + RESOURCE_API_FOLDER.length());

    return path;
  }

  public Map<String, HttpListener4xConfig> getDomainHttpListenerConfigs() {
    return domainHttpListenerConfigs;
  }

  private HttpListener4xConfig getFirstLC() {
    List<Map.Entry<String, HttpListener4xConfig>> numericPortsList = new ArrayList<>();
    List<Map.Entry<String, HttpListener4xConfig>> nonNumericPortsList = new ArrayList<>();

    for (Map.Entry<String, HttpListener4xConfig> entry : domainHttpListenerConfigs.entrySet()) {
      if (StringUtils.isNumeric(entry.getValue().getPort())) {
        numericPortsList.add(entry);
      } else {
        nonNumericPortsList.add(entry);
      }
    }
    Collections.sort(numericPortsList, new Comparator<Map.Entry<String, HttpListener4xConfig>>() {

      @Override
      public int compare(Map.Entry<String, HttpListener4xConfig> o1, Map.Entry<String, HttpListener4xConfig> o2) {
        Integer i1 = Integer.parseInt(o1.getValue().getPort());
        Integer i2 = Integer.parseInt(o2.getValue().getPort());
        return i1.compareTo(i2);
      }
    });
    numericPortsList.addAll(nonNumericPortsList);
    return numericPortsList.get(0).getValue();
  }
}
