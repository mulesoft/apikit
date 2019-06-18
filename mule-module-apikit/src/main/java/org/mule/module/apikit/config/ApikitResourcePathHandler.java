/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.config;


import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Optional;

public class ApikitResourcePathHandler {
  private String pathToResource;
  private Optional<MimeType> contentType;

  public ApikitResourcePathHandler(String pathToResource) {
    this.pathToResource = pathToResource;
    this.contentType = Optional.empty();
  }

  public ApikitResourcePathHandler(String pathToResource, String contentType) {
    this.pathToResource = pathToResource;
    Optional<MimeType> optional;
    try {
      MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
      optional = Optional.of(mimeType);
    } catch (InvalidMimeTypeException e) {
      optional = Optional.empty();
    }
    this.contentType = optional;
  }

  public static ApikitResourcePathHandler parse(String completePath) {
    String[] parts = completePath.split(":");

    switch (parts.length) {
      case 2:
        return new ApikitResourcePathHandler(completePath);
      case 3:
        return new ApikitResourcePathHandler(parts[0] + ":" + parts[1], parts[2]);
      default:
        throw new IllegalArgumentException();
    }

  }

  public String getPathToResource() {
    return pathToResource;
  }

  public Optional<MimeType> getContentType() {
    return contentType;
  }

  public boolean equals(ApikitResourcePathHandler other) {
    if (!this.pathToResource.equals(other.pathToResource)) {
      return false;
    }

    return this.contentType.equals(other.contentType);
  }

  public String getCompletePath() {
    return contentType.isPresent() ? pathToResource + ":" + contentType.get() : pathToResource;
  }
}