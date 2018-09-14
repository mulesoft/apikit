/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import java.io.InputStream;
import javax.annotation.Nullable;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserWrapperV2 implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperV2.class);

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV2(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public IRaml build() {
    return ParserV2Utils.build(new org.raml.v2.api.loader.ResourceLoader() {

      @Nullable
      @Override
      public InputStream fetchResource(String s) {
        return resourceLoader.getRamlResource(s);
      }
    }, ramlPath);
  }

}
