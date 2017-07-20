/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.console;

import org.apache.commons.io.FilenameUtils;
import org.mule.extension.http.api.HttpHeaders;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

public class ConsoleResource implements Resource {

  byte[] content;
  String path;

  public ConsoleResource(byte[] content, String path) {
    this.content = content;
    this.path = path;
  }

  public byte[] getContent() {
    return content;
  }

  public MultiMap<String, String> getHeaders() {
    MediaType mediaType = getMediaType();
    MultiMap<String, String> headers = new MultiMap<>();
    headers.put(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

    if (mediaType.equals(MediaType.HTML)) {
      headers.put(HttpHeaders.Names.EXPIRES, "-1");
    }

    return headers;
  }

  /**
   * Gets Media-Type according to the type of the file we have to send back
   * 
   * @return The MediaType corresponding to the path
   */
  public MediaType getMediaType() {
    String extension = FilenameUtils.getExtension(path);

    if (extension.endsWith("html"))
      return MediaType.HTML;
    if (extension.endsWith("js"))
      return MediaType.create("application", "x-javascript");

    // Default MediaType
    return MediaType.BINARY;
  }
}
