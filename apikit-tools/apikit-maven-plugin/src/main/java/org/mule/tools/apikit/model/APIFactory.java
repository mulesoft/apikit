/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

public class APIFactory
{
    private Map<File, API> apis = new HashMap<File, API>();

    public API createAPIBinding(File yamlFile, File xmlFile, APIKitConfig config, HttpListenerConfig httpListenerConfig, String path)
    {
        Validate.notNull(yamlFile);
        if(apis.containsKey(yamlFile))
        {
            API api = apis.get(yamlFile);
            if(api.getXmlFile() == null && xmlFile != null)
            {
                api.setXmlFile(xmlFile);
            }
            api.setPath(path);
            api.setConfig(config);
            if (httpListenerConfig == null)
            {
                api.setHttpListenerConfig(new HttpListenerConfig.Builder().build());
            }
            return api;
        }

        API api = new API(yamlFile, xmlFile, httpListenerConfig, path);
        apis.put(yamlFile, api);
        return api;
    }
}
