package org.mule.amf.impl.model;

class MediaType {

  static final String APPLICATION_JSON = "application/json";
  static final String APPLICATION_XML = "application/xml";
  static final String APPLICATION_YAML = "application/yaml";

  private MediaType() {}

  static String getMimeTypeForValue(String value) {
    final String trim = value.trim();

    if (trim.startsWith("{") || trim.startsWith("["))
      return APPLICATION_JSON;

    if (trim.startsWith("<") && !trim.startsWith("<<"))
      return APPLICATION_XML;

    return APPLICATION_YAML;
  }
}
