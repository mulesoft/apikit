/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import org.mule.amf.impl.loader.ExchangeDependencyResourceLoader;
import org.mule.amf.impl.model.AmfImpl;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.IRaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static amf.ProfileNames.AMF;
import static java.util.stream.Collectors.joining;
import static org.mule.amf.impl.DocumentParser.getParserForApi;
import static org.mule.amf.impl.DocumentParser.getWebApi;

public class ParserWrapperAmf implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperAmf.class);

  private final URI apiUri;
  private final Environment environment;

  private ParserWrapperAmf(URI uri, Environment environment) {
    this.apiUri = uri;
    this.environment = environment;
  }

  public static ParserWrapperAmf create(String apiPath) {
    try {
      final URI uri = getPathAsUri(apiPath);
      AMF.init().get();
      return new ParserWrapperAmf(uri, buildEnvironment(uri));
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  private static Environment buildEnvironment(URI uri) {
    final String rootDir = new File(uri).isDirectory() ? new File(uri).getPath() : new File(uri).getParent();
    return DefaultEnvironment.apply().add(new ExchangeDependencyResourceLoader(rootDir));
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

  @Override
  public void validate() {
    final Parser parser = getParserForApi(apiUri, environment);

    getWebApi(parser, apiUri);

    final ValidationReport validationReport;
    try {
      validationReport = parser.reportValidation(AMF()).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error parsing API: " + e.getMessage(), e);
    }

    if (!validationReport.conforms()) {
      final String errorMessge = "Invalid API descriptor -- errors found: " +
          validationReport.results().size() +
          "\n\n" +
          validationReport.results().stream().map(ValidationResult::message).collect(joining("\n"));

      throw new RuntimeException(errorMessge);
    }
  }

  @Override
  public IRaml build() {
    final WebApi webApi = getWebApi(apiUri, environment);
    return new AmfImpl(webApi);
  }

  @Override
  public String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String dump(IRaml api, String newBaseUri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRamlUpdater getRamlUpdater(IRaml api) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateBaseUri(IRaml api, String baseUri) {}
}
