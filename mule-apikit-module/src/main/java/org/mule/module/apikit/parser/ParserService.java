/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.mule.module.apikit.injector.RamlUpdater;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;

import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserService {

  private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

  private final String ramlPath;
  private ResourceLoader resourceLoaderV2;
  private ParserWrapper parserWrapper;
  private boolean parserV2;

  public ParserService(String ramlPath) {
    this.ramlPath = ramlPath;
    resourceLoaderV2 = new DefaultResourceLoader();
    checkParserVersion();
    setupParserWrapper(ramlPath);
  }

  public boolean isParserV2() {
    return parserV2;
  }

  private void checkParserVersion() {
    InputStream content = resourceLoaderV2.fetchResource(ramlPath);
    if (content != null) {
      String dump = StreamUtils.toString(content);
      parserV2 = ParserV2Utils.useParserV2(dump);
    }
    logger.debug("Using parser " + (parserV2 ? "V2" : "V1"));
  }

  private void setupParserWrapper(String ramlPath) {
    if (parserV2) {
      parserWrapper = new ParserWrapperV2(ramlPath);
    } else {
      parserWrapper = new ParserWrapperV1(ramlPath);
    }
  }

  public void validateRaml() {
    parserWrapper.validate();
  }

  public IRaml build() {
    return parserWrapper.build();
  }

  public RamlUpdater getRamlUpdater(IRaml api) {
    return parserWrapper.getRamlUpdater(api);
  }

  public String dumpRaml(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    return parserWrapper.dump(ramlContent, api, oldSchemeHostPort, newSchemeHostPort);
  }

  public String dumpRaml(IRaml api, String newBaseUri) {
    return parserWrapper.dump(api, newBaseUri);
  }

  public String dumpRaml(IRaml api) {
    return parserWrapper.dump(api, null);
  }

  public void updateBaseUri(IRaml api, String baseUri) {
    parserWrapper.updateBaseUri(api, baseUri);
  }
}
