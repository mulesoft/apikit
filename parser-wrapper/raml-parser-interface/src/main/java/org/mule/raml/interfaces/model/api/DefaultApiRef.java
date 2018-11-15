/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.api;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

class DefaultApiRef implements ApiRef {

  private String location;

  DefaultApiRef(final String location) {
    this.location = location;
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
