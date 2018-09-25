/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.implv1.model.RamlImplV1;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

class ParserWrapperV1 implements ParserWrapper {

  private final String ramlPath;
  private String content;
  private final ResourceLoader resourceLoader;

  ParserWrapperV1(final String ramlPath, final String content, final ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.content = content;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public IRaml build() {
    RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader::getRamlResource);
    Raml api = builder.build(ramlPath);
    return new RamlImplV1(api);
  }
}

/*

 // Old Version adapted
 final IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
 ramlDocumentBuilder.addPathLookupFirst(ramlPath);
 return ramlDocumentBuilder.build(content, ramlPath);
 
 // Old version
  public class RamlV1Parser { //implements Parseable {
  
  //  @Override
  public IRaml build(File ramlFile, String ramlContent) {
    final IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    ramlDocumentBuilder.addPathLookupFirst(ramlFile.getParentFile().getPath());
    return ramlDocumentBuilder.build(ramlContent, ramlFile.getName());
  }
  
*/

