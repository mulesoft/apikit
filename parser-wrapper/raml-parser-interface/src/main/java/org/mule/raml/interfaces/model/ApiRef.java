/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import java.io.File;
import java.util.Optional;

public class ApiRef {

  private String location;

  private ApiRef(final String location) {
    this.location = location;
  }

  public static ApiRef create(final String location) {
    return new ApiRef(location);
  }

  public String getLocation() {
    return location;
  }

  public boolean isFile() {
    return location.startsWith("file://");
  }

  public Optional<File> toFile() {
    return isFile() ? Optional.of(new File(location)) : Optional.empty();
  }
}
