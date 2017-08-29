/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.util.HashMap;
import java.util.Map;

public class ApikitRegistry {

  private Map<String, Configuration> configMap;

  private Map<String, String> apiSourceMap;

  public void registerConfiguration(Configuration config) {
    if (configMap == null) {
      configMap = new HashMap<>();
    }
    this.configMap.put(config.getName(), config);

    if (apiSourceMap != null) {
      config.getRamlHandler().setApiServer(apiSourceMap.get(config.getName()));


      for (String apiSourceMapItem : apiSourceMap.keySet()) {
        if (configMap.get(apiSourceMapItem) != null) {
          configMap.get(apiSourceMapItem).getRamlHandler().setApiServer(apiSourceMap.get(apiSourceMapItem));
        }
      }

    }
  }

  public Configuration getConfiguration(String configName) {
    return configMap.get(configName);
  }

  public void setApiSource(String configName, String apiSource) {
    if (apiSourceMap == null) {
      apiSourceMap = new HashMap<>();
    }
    apiSourceMap.put(configName, apiSource);


    if (configMap != null) {
      for (String apiSourceMapItem : apiSourceMap.keySet()) {
        if (configMap.get(apiSourceMapItem) != null) {
          configMap.get(apiSourceMapItem).getRamlHandler().setApiServer(apiSourceMap.get(apiSourceMapItem));
        }
      }
    }
  }

}
