/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.mule.tools.apikit.misc.APIKitTools;

import org.apache.commons.lang.StringUtils;

public class HttpListenerConnection {

  public static final String ELEMENT_NAME = "listener-connection";
  public static final String HOST_ATTRIBUTE = "host";
  public static final String PORT_ATTRIBUTE = "port";
  public static final String PROTOCOL_ATTRIBUTE = "protocol";

  private String host;
  private String port;
  private String protocol;

  public static class Builder {

    private String host;
    private String port;
    private String protocol;


    public Builder(final String host, final String port, final String protocol) {
      if (StringUtils.isEmpty(host)) {
        throw new IllegalArgumentException("Name attribute cannot be null or empty");
      }
      if (StringUtils.isEmpty(port)) {
        throw new IllegalArgumentException("Host attribute cannot be null or empty");
      }
      this.host = host;
      this.port = port;
      this.protocol = protocol;
    }

    public Builder() {
      this.host = APIKitTools.getHostFromUri(API.DEFAULT_BASE_URI);
      this.port = APIKitTools.getPortFromUri(API.DEFAULT_BASE_URI);;
      this.protocol = API.DEFAULT_PROTOCOL;
    }

    public Builder setHost(String host) {
      this.host = host;
      return this;
    }

    public Builder setPort(String port) {
      this.port = port;
      return this;
    }

    public Builder setProtocol(String protocol) {
      this.protocol = protocol;
      return this;
    }

    public HttpListenerConnection build() {
      return new HttpListenerConnection(this.host, this.port, this.protocol);
    }
  }

  public HttpListenerConnection(final String host,
                                final String port,
                                final String protocol) {
    this.host = host;
    this.port = port;
    this.protocol = protocol;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public String getProtocol() {
    return protocol;
  }
}
