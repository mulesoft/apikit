/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.mule.module.apikit.parser.Parser.AMF;
import static org.mule.module.apikit.parser.Parser.RAML_V1;
import static org.mule.module.apikit.parser.Parser.RAML_V2;

public class ParserService {

  private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

  private final String ramlPath;
  private ResourceLoader resourceLoaderV2;
  private ParserWrapper parserWrapper;
  private boolean amfParserEnabled;

  private Parser parser;

  public ParserService(String ramlPath, boolean amfParserEnabled) {
    this.ramlPath = ramlPath;
    this.amfParserEnabled = amfParserEnabled;

    resourceLoaderV2 = new DefaultResourceLoader();
    checkParserVersion();
    parserWrapper = getParserWrapper(ramlPath);
  }

  /**
   * @deprecated use getParser() and getApiVendor() instead.
   */
  @Deprecated
  public boolean isParserV2() {
    return parser == RAML_V2 || parser == AMF;
  }

  public Parser getParser() {
    return parser;
  }

  public ApiVendor getApiVendor() {
    return parserWrapper.getApiVendor();
  }

  private void checkParserVersion() {
    if (amfParserEnabled) {
      parser = AMF;
    } else {
      InputStream content = resourceLoaderV2.fetchResource(ramlPath);
      if (content != null) {
        String dump = StreamUtils.toString(content);
        parser = ParserV2Utils.useParserV2(dump) ? RAML_V2 : RAML_V1;
      }
    }
    logger.debug("Using parser " + parser);
  }

  private ParserWrapper getParserWrapper(String ramlPath) {
    switch (parser) {
      case RAML_V1:
        return new ParserWrapperV1(ramlPath);
      case RAML_V2:
        return new ParserWrapperV2(ramlPath);
      default:
        return ParserWrapperAmf.create(getPathAsUri(ramlPath));
    }
  }

  public void validateRaml() {
    parserWrapper.validate();
  }

  public IRaml build() {
    return parserWrapper.build();
  }

  public IRamlUpdater getRamlUpdater(IRaml api) {
    return parserWrapper.getRamlUpdater(api);
  }

  public String dumpRaml(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    return parserWrapper.dump(ramlContent, api, oldSchemeHostPort, newSchemeHostPort);
  }

  public String dumpRaml(IRaml api, String newBaseUri) {
    return parserWrapper.dump(api, newBaseUri);
  }

  public String getAmfModel() {
    return (parserWrapper instanceof ParserWrapperAmf) ? ((ParserWrapperAmf) parserWrapper).getAmfModel() : "";
  }

  public String dumpRaml(IRaml api) {
    return parserWrapper.dump(api, null);
  }

  public void updateBaseUri(IRaml api, String baseUri) {
    parserWrapper.updateBaseUri(api, baseUri);
  }

  private static URI getPathAsUri(String path) {
    try {
      final URI uri = new URI(path);
      if (uri.isAbsolute())
        return uri;
      else {
        //It means that it's a file
        return getUriFromFile(path);
      }
    } catch (URISyntaxException e) {
      return getUriFromFile(path);
    }
  }

  private static URI getUriFromFile(String path) {
    final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

    if (resource != null) {
      try {
        return resource.toURI();
      } catch (URISyntaxException e1) {
        throw new RuntimeException("Couldn't load api in location: " + path);
      }
    } else
      throw new RuntimeException("Couldn't load api in location: " + path);
  }
}
