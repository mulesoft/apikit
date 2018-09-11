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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class APIFactory {

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

  public API createAPIBinding(String ramlFileName, File xmlFile, String baseUri, String path, APIKitConfig config,
                              HttpListener4xConfig httpListenerConfig) {
    Validate.notNull(ramlFileName);
    if (apis.containsKey(ramlFileName)) {
      API api = apis.get(ramlFileName);
      if (api.getXmlFile() == null && xmlFile != null) {
        api.setXmlFile(xmlFile);
      }
      return api;
    }
    API api = new API(ramlFileName, xmlFile, baseUri, path, config);
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
    apis.put(ramlFileName, api);
    return api;
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
