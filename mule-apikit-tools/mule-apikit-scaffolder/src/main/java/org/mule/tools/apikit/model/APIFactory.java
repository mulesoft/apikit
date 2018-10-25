/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import joptsimple.internal.Strings;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static joptsimple.internal.Strings.isNullOrEmpty;
import static org.mule.tools.apikit.model.APIKitConfig.DEFAULT_CONFIG_NAME;

public class APIFactory {

  private Map<File, API> apis = new HashMap<File, API>();
  private Map<String, HttpListener4xConfig> domainHttpListenerConfigs = new HashMap<>();

  public APIFactory(Map<String, HttpListener4xConfig> domainHttpListenerConfigs) {
    this.domainHttpListenerConfigs.putAll(domainHttpListenerConfigs);
  }

  public APIFactory() {}

  public API createAPIBindingInboundEndpoint(File ramlFile, File xmlFile, String baseUri, String path, APIKitConfig config) {
    return createAPIBinding(ramlFile, xmlFile, baseUri, path, config, null);
  }

  public API createAPIBinding(File ramlFile, File xmlFile, String baseUri, String path, APIKitConfig config,
                              HttpListener4xConfig httpListenerConfig) {
    Validate.notNull(ramlFile);
    if (apis.containsKey(ramlFile)) {
      API api = apis.get(ramlFile);
      if (api.getXmlFile() == null && xmlFile != null) {
        api.setXmlFile(xmlFile);
      }
      return api;
    }
    API api = new API(buildApiId(ramlFile), ramlFile, xmlFile, baseUri, path, config);
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
    apis.put(ramlFile, api);
    return api;
  }

  private String buildApiId(File ramlFile) {
    final String apiId = FilenameUtils.removeExtension(ramlFile.getName()).trim();;


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
