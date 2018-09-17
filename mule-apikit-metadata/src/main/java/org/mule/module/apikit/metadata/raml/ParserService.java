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

class ParserService {

  private final String ramlPath;
  private ResourceLoader resourceLoader;
  private ParserWrapper parserWrapper;
  private boolean isParserV2;

  ParserService(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
    isParserV2 = checkParserVersion();
    parserWrapper = parserWrapper(ramlPath, isParserV2);
  }

  private boolean checkParserVersion() {
    InputStream content = resourceLoader.getRamlResource(ramlPath);
    final String dump = StreamUtils.toString(content);
    return ParserV2Utils.useParserV2(dump);
  }

  private ParserWrapper parserWrapper(String ramlPath, final boolean isParserV2) {
    return isParserV2 ? new ParserWrapperV2(ramlPath, resourceLoader) : new ParserWrapperV1(ramlPath, resourceLoader);
  }

  public IRaml build() {
    return parserWrapper.build();
  }
}
