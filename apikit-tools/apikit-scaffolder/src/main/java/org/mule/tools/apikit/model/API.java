/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class API {
    public static final int DEFAULT_PORT = 8081;
    public static final String DEFAULT_BASE_URI = "http://0.0.0.0:" + DEFAULT_PORT + "/api";
    public static final String DEFAULT_BASE_PATH = "/";
    public static final String DEFAULT_CONSOLE_PATH = "/console/*";
    public static final String DEFAULT_CONSOLE_PATH_INBOUND = "http://0.0.0.0:" + DEFAULT_PORT + "/console";

    private APIKitConfig config;
    private HttpListenerConfig httpListenerConfig;
    private String path;

    private String baseUri;
    private File xmlFile;
    private File ramlFile;
    private String id;
    private Boolean useInboundEndpoint;


    public API(File ramlFile, File xmlFile, String baseUri, String path) {
        this.path = path;
        this.ramlFile = ramlFile;
        this.xmlFile = xmlFile;
        this.baseUri = baseUri;
        id = FilenameUtils.removeExtension(ramlFile.getName()).trim();
    }

    public API(File ramlFile, File xmlFile, String baseUri, String path, APIKitConfig config) {
        this(ramlFile, xmlFile, baseUri, path);
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
            xmlFile = new File(rootDirectory,
                    FilenameUtils.getBaseName(
                            ramlFile.getAbsolutePath()) + ".xml");
        }
        return xmlFile;
    }

    public File getRamlFile() {
        return ramlFile;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public HttpListenerConfig getHttpListenerConfig() {
        return httpListenerConfig;
    }

    public APIKitConfig getConfig() {
        return config;
    }

    public void setConfig(APIKitConfig config) {
        this.config = config;
    }

    public void setHttpListenerConfig(HttpListenerConfig httpListenerConfig) {
        this.httpListenerConfig = httpListenerConfig;
    }

    public void setDefaultAPIKitConfig() {
        config = new APIKitConfig.Builder(ramlFile.getName()).setName(id + "-" + APIKitConfig.DEFAULT_CONFIG_NAME).build();
    }

    public void setDefaultHttpListenerConfig()
    {
        String httpListenerConfigName = id == null? HttpListenerConfig.DEFAULT_CONFIG_NAME : id + "-" + HttpListenerConfig.DEFAULT_CONFIG_NAME;
        httpListenerConfig = new HttpListenerConfig.Builder(httpListenerConfigName, API.DEFAULT_BASE_URI).build();
    }

    public Boolean useInboundEndpoint()
    {
        return useInboundEndpoint;
    }
    public boolean setUseInboundEndpoint(Boolean useInboundEndpoint)
    {
        return this.useInboundEndpoint = useInboundEndpoint;
    }
    public String getBaseUri()
    {
        return baseUri;
    }

    public void setBaseUri(String baseUri)
    {
        this.baseUri = baseUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        API api = (API) o;

        if (!ramlFile.equals(api.ramlFile)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ramlFile.hashCode();
    }

    public String getId() {
        return id;
    }

}
