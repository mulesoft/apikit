/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.client.model.domain.WebApi;
import java.io.File;
import java.net.URL;
import org.mule.amf.impl.model.ApiImpl;
import org.mule.raml.interfaces.model.IRaml;

public class ParserApiUtils {

  public static IRaml build(final String api) {
    final WebApi webApi = webApi(api);
    return new ApiImpl(webApi);
  }

  private static WebApi webApi(final String resource) {
    final URL url = ParserApiUtils.class.getResource(resource);
    final File file = new File(url.getFile());
    return DocumentParser.getWebApi(file);
  }
}
