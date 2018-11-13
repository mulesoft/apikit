/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

public enum HttpRequestAttributesFields {

  ATTRIBUTES_QUERY_PARAMS("queryParams"),
  ATTRIBUTES_HEADERS("headers"),
  ATTRIBUTES_URI_PARAMS("uriParams"),
  ATTRIBUTES_LISTENER_PATH("listenerPath"),
  ATTRIBUTES_RELATIVE_PATH("relativePath"),
  ATTRIBUTES_VERSION("version"),
  ATTRIBUTES_SCHEME("scheme"),
  ATTRIBUTES_METHOD("method"),
  ATTRIBUTES_REQUEST_URI("requestUri"),
  ATTRIBUTES_QUERY_STRING("queryString"),
  ATTRIBUTES_REMOTE_ADDRESS("remoteAddress"),
  ATTRIBUTES_CLIENT_CERTIFICATE("clientCertificate"),
  ATTRIBUTES_REQUEST_PATH("requestPath"),
  ATTRIBUTES_LOCAL_ADDRESS("localAddress");

  private String name;

  HttpRequestAttributesFields(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
