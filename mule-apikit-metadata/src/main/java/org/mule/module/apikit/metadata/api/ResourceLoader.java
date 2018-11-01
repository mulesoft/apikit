/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.api;

import java.io.InputStream;

/**
 * Represents a way of getting resources from the application
 */
public interface ResourceLoader {

  /**
   * Gets the root RAML File
   *
   * @param relativePath Location of the root RAML file relative to the /mule/resources/api folder or a resource:: in case
   *                     when the API is defined as a dependency (API sync)
   * @return {@link InputStream} to the RAML resource
   */
  InputStream getResource(String relativePath);

}
