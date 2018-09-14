/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import java.io.InputStream;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.internal.utils.StreamUtils;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;

public class ParserService {

  private final String ramlPath;
  private ResourceLoader resourceLoader;
  private ParserWrapper parserWrapper;
  private boolean parserV2;

  public ParserService(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
    checkParserVersion();
    setupParserWrapper(ramlPath);
  }

  private void checkParserVersion() {
    InputStream content = resourceLoader.getRamlResource(ramlPath);
    if (content != null) {
      String dump = StreamUtils.toString(content);
      parserV2 = ParserV2Utils.useParserV2(dump);
    }
  }

  private void setupParserWrapper(String ramlPath) {
    if (parserV2) {
      parserWrapper = new ParserWrapperV2(ramlPath, resourceLoader);
    } else {
      parserWrapper = new ParserWrapperV1(ramlPath, resourceLoader);
    }
  }

  public IRaml build() {
    return parserWrapper.build();
  }
}
