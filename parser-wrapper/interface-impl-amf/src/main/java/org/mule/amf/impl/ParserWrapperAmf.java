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
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import amf.client.render.AmfGraphRenderer;
import amf.client.render.Oas20Renderer;
import amf.client.render.Raml08Renderer;
import amf.client.render.Raml10Renderer;
import amf.client.render.Renderer;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.core.remote.Vendor;
import org.mule.amf.impl.loader.ExchangeDependencyResourceLoader;
import org.mule.amf.impl.model.AmfImpl;
import org.mule.amf.impl.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.DefaultValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static amf.ProfileNames.AMF;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.mule.amf.impl.DocumentParser.getParserForApi;
import static org.mule.amf.impl.DocumentParser.getWebApi;
import static org.mule.raml.interfaces.common.RamlUtils.replaceBaseUri;

public class ParserWrapperAmf implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperAmf.class);

  private final Parser parser;
  private final Document document;
  private final WebApi webApi;
  private final ApiVendor apiVendor;

  private static final String VENDOR_RAML_08 = "raml 0.8";
  private static final String VENDOR_RAML_10 = "raml 1.0";
  private static final String VENDOR_OAS_20 = "oas 2.0";
  private static final boolean VALIDATE = true;

  private ParserWrapperAmf(final URI uri, Environment environment, boolean validate) {
    parser = getParserForApi(uri, environment);
    document = DocumentParser.parseFile(parser, uri, validate);
    webApi = getWebApi(parser, uri);
    final Option<Vendor> vendor = webApi.sourceVendor();
    apiVendor = vendor.isDefined() ? getApiVendor(vendor.get()) : ApiVendor.RAML_10;
  }

  private static ApiVendor getApiVendor(final Vendor vendor) {

    final ApiVendor result;

    final String name = vendor.name();
    switch (name) {
      case VENDOR_OAS_20:
        result = ApiVendor.OAS_20;
        break;
      case VENDOR_RAML_08:
        result = ApiVendor.RAML_08;
        break;
      default:
        result = ApiVendor.RAML_10;
        break;
    }
    return result;
  }

  public static ParserWrapperAmf create(URI apiUri) {
    return create(apiUri, buildEnvironment(apiUri));
  }

  public static ParserWrapperAmf create(URI apiUri, Environment environment) {
    return create(apiUri, environment, VALIDATE);
  }

  public static ParserWrapperAmf create(URI apiUri, Environment environment, boolean validate) {
    try {
      AMF.init().get();
      return new ParserWrapperAmf(apiUri, environment, validate);
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  private static Environment buildEnvironment(URI uri) {
    Environment environment = DefaultEnvironment.apply();
    if (uri.getScheme() != null && uri.getScheme().startsWith("file")) {
      final File file = new File(uri);
      final String rootDir = file.isDirectory() ? file.getPath() : file.getParent();
      environment = environment.add(new ExchangeDependencyResourceLoader(rootDir));
    }
    return environment;
  }

  @Override
  public ApiVendor getApiVendor() {
    return apiVendor;
  }

  @Override
  public void validate() {

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
  public IValidationReport validationReport() {
    final ValidationReport validationReport;
    try {
      validationReport = parser.reportValidation(AMF()).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error parsing API: " + e.getMessage(), e);
    }

    List<IValidationResult> results;
    if (!validationReport.conforms())
      results = validationReport.results().stream().map(ValidationResultImpl::new).collect(toList());
    else
      results = emptyList();

    return new DefaultValidationReport(results);
  }

  @Override
  public IRaml build() {
    return new AmfImpl(webApi);
  }

  @Override
  public String dump(final String ramlContent, final IRaml api, final String oldBaseUri, final String newBaseUri) {
    return replaceBaseUri(ramlContent, newBaseUri);
  }

  @Override

  public String dump(final IRaml api, final String newBaseUri) {
    String dump = dumpRaml(api);
    if (newBaseUri != null) {
      dump = replaceBaseUri(dump, newBaseUri);
    }
    return dump;
  }

  @Override
  public IRamlUpdater getRamlUpdater(final IRaml api) {
    throw new UnsupportedOperationException();
  }

  private String dumpRaml(final IRaml api) {
    return renderApi();
  }

  @Override
  public void updateBaseUri(IRaml api, String baseUri) {}

  public String getAmfModel() {
    try {
      return new AmfGraphRenderer().generateString(document).get();
    } catch (InterruptedException | ExecutionException e) {
      return e.getMessage();
    }
  }

  private String renderApi() {

    final Renderer renderer;
    switch (apiVendor) {
      case RAML_08:
        renderer = new Raml08Renderer();
        break;
      case OAS_20:
        renderer = new Oas20Renderer();
        break;
      default:
        renderer = new Raml10Renderer();
        break;
    }

    try {
      return renderer.generateString(document).get();
    } catch (final InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return "";
    }
  }
}
