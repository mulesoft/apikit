/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import static java.io.File.separator;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.mule.tools.apikit.model.API.DEFAULT_BASE_PATH;
import static org.mule.tools.apikit.model.API.DEFAULT_HOST;
import static org.mule.tools.apikit.model.API.DEFAULT_PROTOCOL;
import static org.mule.tools.apikit.model.APIKitConfig.DEFAULT_CONFIG_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.FileUtils;
import org.mule.raml.interfaces.common.APISyncUtils;

public class APIFactory {

  private static final String RESOURCE_API_FOLDER =
      "src" + separator + "main" + separator + "resources" + separator + "api" + separator;

  private Map<String, API> apis = new HashMap<>();
  private final Map<String, HttpListener4xConfig> httpListenerConfigs = new HashMap<>();

  public APIFactory(Map<String, HttpListener4xConfig> httpListenerConfigs) {
    this.httpListenerConfigs.putAll(httpListenerConfigs);
  }

  public APIFactory() {}

  public API createAPIBindingInboundEndpoint(
                                             String apiFileName, File xmlFile, String baseUri, String path, APIKitConfig config) {
    return createAPIBinding(apiFileName, xmlFile, baseUri, path, config, null);
  }

  public API createAPIBinding(
                              String apiFilePath,
                              File xmlFile,
                              String baseUri,
                              String path,
                              APIKitConfig config,
                              HttpListener4xConfig httpListenerConfig) {

    Validate.notNull(apiFilePath);
    final String relativePath = getRelativePath(apiFilePath);
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
      final HttpListener4xConfig availableConfig = getAvailableLCForPath(path, httpListenerConfigs);
      if (availableConfig != null) {
        api.setHttpListenerConfig(availableConfig);
      } else {
        final HttpListener4xConfig defaultHttpListenerConfig = buildDefaultHttpListenerConfig(id);
        httpListenerConfigs.put(defaultHttpListenerConfig.getName(), defaultHttpListenerConfig);
        api.setHttpListenerConfig(defaultHttpListenerConfig);
      }
    } else {
      api.setHttpListenerConfig(httpListenerConfig);
    }
    if (config != null) {
      config.setApi(apiFilePath);
    }
    api.setConfig(config);
    apis.put(relativePath, api);
    return api;
  }

  private HttpListener4xConfig buildDefaultHttpListenerConfig(String id) {
    final String nextPort = getNextPort(httpListenerConfigs);
    final HttpListenerConnection listenerConnection = buildDefaultHttpListenerConnection(nextPort);
    String httpListenerConfigName =
        id == null
            ? HttpListener4xConfig.DEFAULT_CONFIG_NAME
            : id + "-" + HttpListener4xConfig.DEFAULT_CONFIG_NAME;
    return new HttpListener4xConfig(httpListenerConfigName, DEFAULT_BASE_PATH, listenerConnection);
  }

  private static String getNextPort(Map<String, HttpListener4xConfig> httpListenerConfigs) {
    final List<Map.Entry<String, HttpListener4xConfig>> numericPortListeners =
        getNumericPortListenersAsList(httpListenerConfigs);
    if (numericPortListeners.size() > 0) {
      final String greaterPort =
          numericPortListeners.get(numericPortListeners.size() - 1).getValue().getPort();
      return String.valueOf(Integer.parseInt(greaterPort) + 1);
    }

    return "8081";
  }

  private HttpListenerConnection buildDefaultHttpListenerConnection(String port) {
    return new HttpListenerConnection.Builder()
        .setHost(DEFAULT_HOST)
        .setPort(port)
        .setProtocol(DEFAULT_PROTOCOL)
        .build();
  }

  private String buildApiId(String ramlFilePath) {
    final String apiId;

    if (APISyncUtils.isSyncProtocol(ramlFilePath))
      apiId = FilenameUtils.removeExtension(APISyncUtils.getFileName(ramlFilePath));
    else
      apiId = FilenameUtils.removeExtension(FileUtils.basename(ramlFilePath)).trim();

    final List<String> apiIds = apis.values().stream().map(API::getId).collect(toList());

    final List<String> configNames =
        apis.values().stream()
            .filter(a -> a.getConfig() != null)
            .map(a -> a.getConfig().getName())
            .collect(toList());

    final List<String> httpConfigNames =
        apis.values().stream()
            .filter(a -> a.getHttpListenerConfig() != null)
            .map(a -> a.getHttpListenerConfig().getName())
            .collect(toList());

    int count = 0;
    String id;
    do {
      count++;
      id = (count > 1 ? apiId + "-" + count : apiId);
    } while (apiIds.contains(id)
        || configNames.contains(id + "-" + DEFAULT_CONFIG_NAME)
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

  public Map<String, HttpListener4xConfig> getHttpListenerConfigs() {
    return httpListenerConfigs;
  }

  private HttpListener4xConfig getAvailableLCForPath(
                                                     String path, Map<String, HttpListener4xConfig> httpListenerConfigs) {
    if (httpListenerConfigs.size() <= 0)
      return null;

    final List<HttpListener4xConfig> usedListeners =
        apis.entrySet().stream()
            .filter(
                    e -> {
                      final String apiPath = e.getValue().getPath();
                      return path.equals(apiPath) || (path + "/*").equals(apiPath);
                    })
            .map(e -> e.getValue().getHttpListenerConfig())
            .collect(toList());

    final List<Map.Entry<String, HttpListener4xConfig>> availableListeners = new ArrayList<>();
    availableListeners.addAll(getNumericPortListenersAsList(httpListenerConfigs));
    availableListeners.addAll(getNonNumericPortListeners(httpListenerConfigs).entrySet());

    return availableListeners.stream()
        .map(Map.Entry::getValue)
        .filter(l -> !usedListeners.contains(l))
        .findFirst()
        .orElse(null);
  }

  private static Map<String, HttpListener4xConfig> getNumericPortListeners(
                                                                           Map<String, HttpListener4xConfig> httpListenerConfigs) {
    return httpListenerConfigs.entrySet().stream()
        .filter(entry -> StringUtils.isNumeric(entry.getValue().getPort()))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static List<Map.Entry<String, HttpListener4xConfig>> getNumericPortListenersAsList(
                                                                                             Map<String, HttpListener4xConfig> httpListenerConfigs) {
    final List<Map.Entry<String, HttpListener4xConfig>> numericPortsList =
        new ArrayList<>(getNumericPortListeners(httpListenerConfigs).entrySet());

    numericPortsList.sort(
                          (o1, o2) -> {
                            Integer i1 = Integer.parseInt(o1.getValue().getPort());
                            Integer i2 = Integer.parseInt(o2.getValue().getPort());
                            return i1.compareTo(i2);
                          });

    return numericPortsList;
  }

  private static Map<String, HttpListener4xConfig> getNonNumericPortListeners(
                                                                              Map<String, HttpListener4xConfig> httpListenerConfigs) {
    return httpListenerConfigs.entrySet().stream()
        .filter(entry -> !StringUtils.isNumeric(entry.getValue().getPort()))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
