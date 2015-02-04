/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.mule.tools.apikit.misc.APIKitTools;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

public class APIFactory
{
    private Map<File, API> apis = new HashMap<File, API>();


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
            api.setUseInboundEndpoint(useInboundEndpoint);
            if (httpListenerConfig == null && !useInboundEndpoint)
            {
               httpListenerConfig = createHttpListenerConfig(ramlFile, baseUri);
            }
            api.setHttpListenerConfig(httpListenerConfig);

            api.setConfig(config);

            return api;
        }
        API api = new API(ramlFile, xmlFile, baseUri, path, config);
        api.setUseInboundEndpoint(useInboundEndpoint);
        if (httpListenerConfig == null && !useInboundEndpoint)
        {
            httpListenerConfig = createHttpListenerConfig(ramlFile, baseUri);
        }
        api.setHttpListenerConfig(httpListenerConfig);
        apis.put(ramlFile, api);
        return api;
    }


    private HttpListenerConfig createHttpListenerConfig(File ramlFile, String baseUri)
    {
        String id = FilenameUtils.removeExtension(ramlFile.getName()).trim();
        String httpListenerConfigName = id == null? HttpListenerConfig.DEFAULT_CONFIG_NAME : id + "-" + HttpListenerConfig.DEFAULT_CONFIG_NAME;
        return new HttpListenerConfig.Builder(httpListenerConfigName, baseUri).build();

    }

}
