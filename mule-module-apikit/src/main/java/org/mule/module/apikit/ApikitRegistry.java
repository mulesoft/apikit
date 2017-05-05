/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.util.HashMap;
import java.util.Map;

public class ApikitRegistry
{
    private Map<String, Configuration> configMap;

    public void registerConfiguration(Configuration config)
    {
        if (configMap == null)
        {
            configMap = new HashMap<>();
        }
        this.configMap.put(config.getName(), config);
    }

    public Configuration getConfiguration(String configName)
    {
        return configMap.get(configName);
    }


}
