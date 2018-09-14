/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.implv1.model.RamlImplV1;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

public class ParserWrapperV1 implements ParserWrapper {

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV1(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public IRaml build() {
    RamlDocumentBuilder builder = new RamlDocumentBuilder(s -> resourceLoader.getRamlResource(s));
    Raml api = builder.build(ramlPath);
    return new RamlImplV1(api);
  }
}
