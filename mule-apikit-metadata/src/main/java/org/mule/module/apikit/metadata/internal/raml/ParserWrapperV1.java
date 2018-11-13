/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import java.net.URI;
import org.mule.module.apikit.metadata.api.ResourceLoader;
import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv1.loader.ApiSyncResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import javax.annotation.Nullable;
import java.io.InputStream;
import sun.misc.IOUtils;

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
    return ParserV1Utils.build(content, new ApiSyncResourceLoader(ramlPath, adaptResourceLoader(resourceLoader)), ramlPath);
  }

  private org.raml.parser.loader.ResourceLoader adaptResourceLoader(final ResourceLoader resourceLoader) {
    return new org.raml.parser.loader.ResourceLoader() {

      @Nullable
      @Override
      public InputStream fetchResource(String s) {
        final URI uri = resourceLoader.getResource(s);
        return ParserWrapper.toInputStream(uri);
      }
    };
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

