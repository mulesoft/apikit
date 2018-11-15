/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.model.api;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

class URIApiRef implements ApiRef {

  private URI uri;

  URIApiRef(final URI uri) {
    this.uri = uri;
  }

  @Override
  public String getLocation() {
    return uri.toString();
  }

  @Override
  public String getFormat() {
    return FilenameUtils.getExtension(uri.getPath()).toUpperCase();
  }

  @Override
  public InputStream resolve() {
    try {
      return new BufferedInputStream(uri.toURL().openStream());
    } catch (IOException e) {
      return null;
    }
  }
}
