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

import org.apache.commons.lang.Validate;

public class APIFactory
{
    private Map<File, API> apis = new HashMap<File, API>();
    private Map<String, HttpListenerConfig> domainHttpListenerConfigs = new HashMap<>();
    public APIFactory (Map<String, HttpListenerConfig> domainHttpListenerConfigs)
    {
        this.domainHttpListenerConfigs.putAll(domainHttpListenerConfigs);
    }

    public APIFactory ()
    {
    }

    public API createAPIBinding(File ramlFile, File xmlFile, String baseUri, String path, APIKitConfig config)
    {
        return createAPIBinding(ramlFile, xmlFile, baseUri, path, config, null, true);
    }

    public API createAPIBinding(File ramlFile, File xmlFile, String path, APIKitConfig config, HttpListenerConfig httpListenerConfig)
    {
        return createAPIBinding(ramlFile, xmlFile, null, path, config, httpListenerConfig, false);
    }

    public API createAPIBinding(File ramlFile, File xmlFile, String baseUri, String path, APIKitConfig config, HttpListenerConfig httpListenerConfig, Boolean useInboundEndpoint)
    {
        Validate.notNull(ramlFile);
        if(apis.containsKey(ramlFile))
        {
            API api = apis.get(ramlFile);
            if(api.getXmlFile() == null && xmlFile != null)
            {
                api.setXmlFile(xmlFile);
            }
            return api;
        }
        API api = new API(ramlFile, xmlFile, baseUri, path, config);
        api.setUseInboundEndpoint(useInboundEndpoint);
        if (!useInboundEndpoint)
        {
            if (httpListenerConfig == null)
            {
                if (domainHttpListenerConfigs.size() >0)
                {
                    api.setHttpListenerConfig(getFirstLC());
                }
                else
                {
                    api.setDefaultHttpListenerConfig();
                }
            }
            else
            {
                api.setHttpListenerConfig(httpListenerConfig);
            }
        }
        api.setConfig(config);
        apis.put(ramlFile, api);
        return api;
    }

    public Map<String, HttpListenerConfig> getDomainHttpListenerConfigs() {
        return domainHttpListenerConfigs;
    }

    private HttpListenerConfig getFirstLC()
    {
        List<Map.Entry<String,HttpListenerConfig>> list = new ArrayList<>(domainHttpListenerConfigs.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, HttpListenerConfig>>(){
            @Override
            public int compare(Map.Entry<String, HttpListenerConfig> o1, Map.Entry<String, HttpListenerConfig> o2)
            {
                Integer i1 = Integer.parseInt(o1.getValue().getPort());
                Integer i2 = Integer.parseInt(o2.getValue().getPort());
                return i1.compareTo(i2);
            }
        });
        return list.get(0).getValue();
    }
}
