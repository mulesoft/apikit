/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

public class HttpListenerConfig
{
    public static final String ELEMENT_NAME = "listener-config";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String DEFAULT_CONFIG_NAME = "HTTP_Listener_Configuration";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = String.valueOf(API.DEFAULT_PORT);
    public static final String DEFAULT_BASE_PATH = "";


    private String name;
    private String host;
    private String port;
    private String basePath;

    public static class Builder {
        private String name;
        private String host;
        private String port;
        private String basePath;

        public Builder(final String name, final String host, final String port, final String basePath) {
            if(StringUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Name attribute cannot be null or empty");
            }
            if(StringUtils.isEmpty(host)) {
                throw new IllegalArgumentException("Host attribute cannot be null or empty");
            }
            if(StringUtils.isEmpty(port)) {
                throw new IllegalArgumentException("Port attribute cannot be null or empty");
            }
            this.name = name;
            this.host = host;
            this.port = port;
            this.basePath = basePath;
        }


        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(String port) {
            this.port = port;
            return this;
        }

        public Builder setBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder()
        {
            this.name = DEFAULT_CONFIG_NAME;
            this.host = DEFAULT_HOST;
            this.port = DEFAULT_PORT;
            this.basePath = DEFAULT_BASE_PATH;
        }

        public HttpListenerConfig build() {
            return new HttpListenerConfig(this.name, this.host, this.port, this.basePath);
        }
    }

    public HttpListenerConfig(final String name,
                         final String host,
                         final String port,
                         final String basePath) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
    }

    public HttpListenerConfig(final String baseUri){
        URL url;
        try
        {
            url = new URL(baseUri);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException("MalformedURLException", ex);
        }
        name = DEFAULT_CONFIG_NAME;
        host = url.getHost();
        port = String.valueOf(url.getPort() == -1? DEFAULT_PORT : url.getPort());
        basePath = url.getPath();
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public String getUrl() { return "http://" + host + ":" + port + "/" + basePath;}
}
