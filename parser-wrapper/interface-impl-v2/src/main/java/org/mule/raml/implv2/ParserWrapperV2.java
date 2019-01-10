/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import org.mule.raml.implv2.loader.ApiSyncResourceLoader;
import org.mule.raml.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.common.RamlUtils;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.DefaultValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
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
import static org.mule.raml.interfaces.common.APISyncUtils.isSyncProtocol;
import static org.mule.raml.interfaces.common.RamlUtils.replaceBaseUri;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ParserWrapperV2 implements ParserWrapper {

  public static final ResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperV2.class);

  private final String ramlPath;
  private final ResourceLoader resourceLoader;

  public ParserWrapperV2(String ramlPath) {
    this(ramlPath, getResourceLoaderForPath(ramlPath));
  }

  public ParserWrapperV2(String ramlPath, ResourceLoader resourceLoader) {
    this.ramlPath = ramlPath;
    this.resourceLoader = resourceLoader;
  }

  public ParserWrapperV2(String ramlPath, ResourceLoader... resourceLoader) {
    this(ramlPath, new CompositeResourceLoader(resourceLoader));
  }

  public static ResourceLoader getResourceLoaderForPath(String ramlPath) {
    final File ramlFile = fetchRamlFile(ramlPath);

    if (ramlFile != null && ramlFile.getParent() != null) {
      final File ramlFolder = ramlFile.getParentFile();
      return new CompositeResourceLoader(new RootRamlFileResourceLoader(ramlFolder),
                                         DEFAULT_RESOURCE_LOADER,
                                         new FileResourceLoader(ramlFolder.getAbsolutePath()),
                                         new ExchangeDependencyResourceLoader(ramlFolder.getAbsolutePath()));
    } else {
      if (isSyncProtocol(ramlPath)) {
        return new ApiSyncResourceLoader(ramlPath);
      } else {
        return DEFAULT_RESOURCE_LOADER;
      }

    }
  }

  private static File fetchRamlFile(String ramlPath) {
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
  public ApiVendor getApiVendor() {
    return RAML_10; // TODO Support both ApiVendor parsing RAML file
  }

  @Override
  public ParserType getParserType() {
    return ParserType.RAML;
  }

  @Override
  public void validate() {
    List<IValidationResult> errors = ParserV2Utils.validate(resourceLoader, ramlPath);
    if (!errors.isEmpty()) {
      StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
      message.append(errors.size()).append("\n\n");
      for (IValidationResult error : errors) {
        message.append(error.getMessage()).append("\n");
      }
      throw new RuntimeException(message.toString());
    }
  }

  @Override
  public IValidationReport validationReport() {
    final List<IValidationResult> results = ParserV2Utils.validate(resourceLoader, ramlPath);
    return new DefaultValidationReport(results);
  }

  @Override
  public IRaml build() {
    return ParserV2Utils.build(resourceLoader, ramlPath);
  }

  @Override
  public String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    return RamlUtils.replaceBaseUri(ramlContent, newSchemeHostPort);
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
