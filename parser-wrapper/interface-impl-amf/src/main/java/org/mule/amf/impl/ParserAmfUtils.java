/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.client.model.domain.WebApi;
import org.mule.amf.impl.model.AmfImpl;
import org.mule.raml.interfaces.model.IRaml;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ParserAmfUtils {

  public static IRaml build(final String api) {
    final WebApi webApi = webApi(api);
    return new AmfImpl(webApi);
  }

  private static WebApi webApi(final String resource) {
    final URL url = ParserAmfUtils.class.getResource(resource);
    try {
      final URI uri = url.toURI();
      return DocumentParser.getWebApi(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected Error building Api", e);
    }
  }
}
