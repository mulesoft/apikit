/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
