/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Represents a way of getting resources from the application
 */
public interface ResourceLoader {

  /**
   * Gets the root API File
   *
   * @param relativePath Location of the root API file relative to the /mule/resources/api folder or a resource:: in case
   *                     when the API is defined as a dependency (API sync)
   * @return {@link URI} to the API resource
   */
  URI getResource(String relativePath);

  default InputStream getResourceAsStream(String relativePath) {
    URI uri = getResource(relativePath);
    try {
      return uri != null ? uri.toURL().openStream() : null;
    } catch (IOException e) {
      return null;
    }
  }
}
