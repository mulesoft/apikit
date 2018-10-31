/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model;

import java.io.File;
import java.net.URI;
import java.util.Optional;

public interface ApiRef {

  static ApiRef create(final String location) {
    return new ByPath(location);
  }

  static ApiRef create(final URI uri) {
    return new ByURI(uri);
  }

  String getLocation();

  default boolean isFile() {
    return getLocation().startsWith("file://");
  }

  default Optional<File> toFile() {
    return isFile() ? Optional.of(new File(getLocation())) : Optional.empty();
  }

  class ByPath implements ApiRef {

    private String location;

    ByPath(final String location) {
      this.location = location;
    }

    @Override
    public String getLocation() {
      return location;
    }
  }

  class ByURI implements ApiRef {

    private URI uri;

    ByURI(final URI uri) {
      this.uri = uri;
    }

    @Override
    public String getLocation() {
      return uri.toString();
    }
  }
}
