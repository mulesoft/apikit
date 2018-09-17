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

class ParserWrapperV2 implements ParserWrapper {

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  ParserWrapperV2(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public IRaml build() {
    final org.raml.v2.api.loader.ResourceLoader adaptedResourceLoader = new org.raml.v2.api.loader.ResourceLoader() {

      @Nullable
      @Override
      public InputStream fetchResource(String s) {
        return ParserWrapperV2.this.resourceLoader.getRamlResource(s);
      }
    };
    return ParserV2Utils.build(adaptedResourceLoader, ramlPath);
  }


  /* 
  public class RamlV2Parser { 
  
    public IRaml build(File ramlFile, String ramlContent) {
    org.raml.v2.api.loader.ResourceLoader resourceLoader =
        new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(ramlFile.getParentFile().getPath()));
    RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlContent, ramlFile.getPath());
    return wrapApiModel(ramlModelResult);
  }
  
  private static IRaml wrapApiModel(RamlModelResult ramlModelResult) {
    if (ramlModelResult.hasErrors()) {
      throw new RuntimeException("Invalid RAML descriptor.");
    }
    if (ramlModelResult.isVersion08()) {
      return new RamlImpl08V2(ramlModelResult.getApiV08());
    }
    return new RamlImpl10V2(ramlModelResult.getApiV10());
  }
  }
  
   */
}
