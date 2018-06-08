/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import org.apache.commons.lang3.StringUtils;
import org.mule.raml.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.loader.RootRamlFileResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.mule.raml.interfaces.common.RamlUtils.replaceBaseUri;

public class ParserWrapperV2 implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperV2.class);

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV2(String ramlPath) {
    this.ramlPath = ramlPath;

    final File ramlFile = fetchRamlFile(ramlPath);

    if (ramlFile != null && ramlFile.getParent() != null) {
      this.resourceLoader =
          new org.raml.v2.api.loader.CompositeResourceLoader(new RootRamlFileResourceLoader(ramlFile.getParentFile()),
                                                             new DefaultResourceLoader(),
                                                             new ExchangeDependencyResourceLoader(ramlFile.getParentFile()
                                                                 .getAbsolutePath()));
    } else {
      this.resourceLoader = new DefaultResourceLoader();
    }
  }

  private File fetchRamlFile(String ramlPath) {
    return ofNullable(ramlPath)
        .map(p -> Thread.currentThread().getContextClassLoader().getResource(p))
        .filter(ParserWrapperV2::isFile)
        .map(resource -> new File(resource.getFile()))
        .orElse(null);
  }

  private static boolean isFile(URL url) {
    return "file".equals(url.getProtocol());
  }

  @Override
  public void validate() {
    List<String> errors = ParserV2Utils.validate(resourceLoader, ramlPath);
    if (!errors.isEmpty()) {
      StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
      message.append(errors.size()).append("\n\n");
      for (String error : errors) {
        message.append(error).append("\n");
      }
      throw new RuntimeException(message.toString());
    }
  }

  @Override
  public IRaml build() {
    return ParserV2Utils.build(resourceLoader, ramlPath);
  }

  @Override
  public String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    return replaceBaseUri(ramlContent, newSchemeHostPort);
  }

  @Override
  public String dump(IRaml api, String newBaseUri) {
    String dump = dumpRaml(api);
    if (newBaseUri != null) {
      dump = replaceBaseUri(dump, newBaseUri);
    }
    return dump;
  }

  private String dumpRaml(IRaml api) {
    InputStream stream = resourceLoader.fetchResource(ramlPath);
    if (stream == null) {
      throw new RuntimeException("Invalid RAML descriptor");
    }
    return StreamUtils.toString(stream);
  }

  @Override
  public IRamlUpdater getRamlUpdater(IRaml api) {
    throw new UnsupportedOperationException("RAML 1.0 is read only");
  }

  @Override
  public void updateBaseUri(IRaml api, String baseUri) {
    // do nothing, as updates are not supported
    logger.debug("RAML 1.0 parser does not support base uri updates");
  }
}
