/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.mule.module.apikit.metadata.interfaces.Parseable;
import org.mule.raml.implv2.v08.model.RamlImpl08V2;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;

import java.io.File;

public class RamlV2Parser implements Parseable {

  @Override
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
