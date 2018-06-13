/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.client.environment.DefaultEnvironment;
import amf.client.model.domain.WebApi;
import org.mule.amf.impl.model.AmfImpl;
import org.mule.raml.interfaces.model.IRaml;

import java.io.File;
import java.net.URI;

public class ParserAmfUtils {

  public static IRaml build(final File api) {
    final WebApi webApi = webApi(api);
    return new AmfImpl(webApi);
  }

  private static WebApi webApi(final File file) {
    final URI uri = file.toURI();
    return DocumentParser.getWebApi(uri, DefaultEnvironment.apply());
  }
}
