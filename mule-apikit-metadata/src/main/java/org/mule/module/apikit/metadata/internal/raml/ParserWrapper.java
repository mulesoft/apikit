/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import org.mule.raml.interfaces.model.IRaml;

interface ParserWrapper {

  IRaml build();

  static InputStream toInputStream(final URI uri) {

    InputStream inputStream = null;
    try {
      URL url = uri.toURL();
      inputStream = new BufferedInputStream(url.openStream());
    } catch (IOException e) {
      // ignore on resource not found
    }
    return inputStream;
  }
}
