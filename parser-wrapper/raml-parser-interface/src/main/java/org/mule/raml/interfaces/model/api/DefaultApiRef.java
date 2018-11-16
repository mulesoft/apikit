/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.api;


import org.apache.commons.io.FilenameUtils;
import org.mule.raml.interfaces.loader.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

class DefaultApiRef implements ApiRef {

  private String location;
  private Optional<ResourceLoader> resourceLoader;

  DefaultApiRef(final String location) {
    this(location, null);
  }

  DefaultApiRef(final String location, ResourceLoader resourceLoader) {
    this.location = location;
    this.resourceLoader = Optional.ofNullable(resourceLoader);
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public String getFormat() {
    return FilenameUtils.getExtension(location).toUpperCase();
  }

  @Override
  public InputStream resolve() {
    if (resourceLoader.isPresent())
      return resourceLoader.map(loader -> loader.getResourceAsStream(location)).orElse(null);
    else {
      final File file = new File(location);

      if (file.exists()) {
        try {
          return new FileInputStream(file);
        } catch (Exception e) {
          return null;
        }
      } else {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
      }
    }
  }

  @Override
  public Optional<ResourceLoader> getResourceLoader() {
    return resourceLoader;
  }
}
