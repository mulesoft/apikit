package org.mule.module.apikit.metadata;

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
