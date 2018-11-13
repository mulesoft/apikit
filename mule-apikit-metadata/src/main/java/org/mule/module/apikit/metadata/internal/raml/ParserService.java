/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;

class ParserService {

  private ParserWrapper parserWrapper;

  ParserService(final String ramlPath, final ResourceLoader resourceLoader) {
    parserWrapper = parserWrapper(ramlPath, resourceLoader);
  }

  private boolean getParserVersion(final String content) {
    return ParserV2Utils.useParserV2(content);
  }

  private String readResource(final String ramlPath, final ResourceLoader resourceLoader) {
    URI uri = resourceLoader.getResource(ramlPath);

    try {
      return IOUtils.toString(uri);
    } catch (final IOException e) {
      return "";
    }
  }

  private ParserWrapper parserWrapper(final String ramlPath, final ResourceLoader resourceLoader) {
    final String content = readResource(ramlPath, resourceLoader);
    final boolean isParserV2 = getParserVersion(content);
    return isParserV2 ? new ParserWrapperV2(ramlPath, content, resourceLoader)
        : new ParserWrapperV1(ramlPath, content, resourceLoader);
  }

  public IRaml build() {
    return parserWrapper.build();
  }
}
