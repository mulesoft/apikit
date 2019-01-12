/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.client.AMF;
import amf.client.environment.DefaultEnvironment;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.Server;
import amf.client.model.domain.WebApi;
import amf.client.parse.Parser;
import amf.client.render.AmfGraphRenderer;
import amf.client.render.Oas20Renderer;
import amf.client.render.Raml08Renderer;
import amf.client.render.Raml10Renderer;
import amf.client.render.Renderer;
import amf.client.resolve.Raml10Resolver;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import org.mule.amf.impl.loader.ExchangeDependencyResourceLoader;
import org.mule.amf.impl.loader.ProvidedResourceLoader;
import org.mule.amf.impl.model.AmfImpl;
import org.mule.amf.impl.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.mule.raml.interfaces.parser.rule.DefaultValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static amf.ProfileNames.AMF;
import static amf.ProfileNames.OAS;
import static amf.ProfileNames.OAS20;
import static amf.ProfileNames.RAML;
import static amf.ProfileNames.RAML08;
import static amf.ProfileNames.RAML10;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.mule.amf.impl.DocumentParser.getParserForApi;
import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.raml.interfaces.common.RamlUtils.replaceBaseUri;

public class ParserWrapperAmf implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperAmf.class);

  private final ApiRef apiRef;
  private final Parser parser;
  private final WebApi webApi;
  private final ApiVendor apiVendor;
  private final List<String> references;

  private Document consoleModel;

  private ParserWrapperAmf(final ApiRef apiRef, boolean validate) throws ExecutionException, InterruptedException {
    AMF.init().get();
    this.apiRef = apiRef;
    parser = initParser(apiRef);

    final Document document = buildDocument(validate);
    references = getReferences(document.references());
    webApi = DocumentParser.getWebApi(document);
    apiVendor = apiRef.getVendor();
  }

  private static Parser initParser(ApiRef apiRef) {
    final Environment environment = buildEnvironment(apiRef);
    return getParserForApi(apiRef, environment);
  }

  private List<String> getReferences(final List<BaseUnit> references) {

    final List<String> result = new ArrayList<>();
    appendReferences(references, new HashSet<>(), result);
    return result;
  }

  private void appendReferences(final List<BaseUnit> references, final Set<String> alreadyAdded, final List<String> result) {

    for (final BaseUnit reference : references) {
      final String id = reference.id();
      if (!alreadyAdded.contains(id)) {
        final String location = reference.location();
        result.add(location);
        alreadyAdded.add(id);
        appendReferences(reference.references(), alreadyAdded, result);
      }
    }
  }

  public static ParserWrapperAmf create(ApiRef apiRef, boolean validate) throws Exception {
    return new ParserWrapperAmf(apiRef, validate);
  }

  private static URI getPathAsUri(ApiRef apiRef) {
    try {
      final URI uri = new URI(apiRef.getLocation());
      return uri.isAbsolute() ? uri : getUriFromFile(apiRef);
    } catch (URISyntaxException e) {
      return getUriFromFile(apiRef);
    }
  }

  // It means that it's a file
  private static URI getUriFromFile(ApiRef apiRef) {
    final String location = apiRef.getLocation();
    if (apiRef.getResourceLoader().isPresent()) {
      final URI uri = apiRef.getResourceLoader().map(loader -> loader.getResource(location)).orElse(null);
      if (uri != null)
        return uri;
    }

    final File file = new File(location);
    if (file.exists())
      return file.toURI();

    final URL resource = Thread.currentThread().getContextClassLoader().getResource(location);

    if (resource != null) {
      try {
        return resource.toURI();
      } catch (URISyntaxException e1) {
        throw new RuntimeException("Couldn't load api in location: " + location);
      }
    } else
      throw new RuntimeException("Couldn't load api in location: " + location);
  }

  private static Environment buildEnvironment(ApiRef apiRef) {
    final URI uri = getPathAsUri(apiRef);

    Environment environment = DefaultEnvironment.apply();

    if (uri.getScheme() != null && uri.getScheme().startsWith("file")) {
      final File file = new File(uri);
      final String rootDir = file.isDirectory() ? file.getPath() : file.getParent();
      environment = environment.add(new ExchangeDependencyResourceLoader(rootDir));
    }

    if (apiRef.getResourceLoader().isPresent()) {
      environment = environment.add(new ProvidedResourceLoader(apiRef.getResourceLoader().get()));
    }

    return environment;
  }

  @Override
  public ApiVendor getApiVendor() {
    return apiVendor;
  }

  @Override
  public ParserType getParserType() {
    return ParserType.AMF;
  }

  public WebApi getWebApi() {
    return webApi;
  }

  @Override
  public void validate() {

    final ValidationReport validationReport = generateValidationReport();

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
    final ValidationReport validationReport = generateValidationReport();

    List<IValidationResult> results;
    if (!validationReport.conforms())
      results = validationReport.results().stream().map(ValidationResultImpl::new).collect(toList());
    else
      results = emptyList();

    return new DefaultValidationReport(results);
  }

  private ValidationReport generateValidationReport() {
    final ValidationReport validationReport;
    try {
      validationReport = parser.reportValidation(apiVendorToProfileName(apiVendor)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error parsing API: " + e.getMessage(), e);
    }
    return validationReport;
  }

  private ProfileName apiVendorToProfileName(ApiVendor apiVendor) {
    switch (apiVendor) {
      case OAS:
        return OAS();
      case OAS_20:
        return OAS20();
      case RAML:
        return RAML();
      case RAML_08:
        return RAML08();
      case RAML_10:
        return RAML10();
      default:
        return AMF();
    }
  }


  @Override
  public IRaml build() {
    return new AmfImpl(webApi, references);
  }

  @Override
  public String dump(final String ramlContent, final IRaml api, final String oldBaseUri, final String newBaseUri) {
    return replaceBaseUri(ramlContent, newBaseUri);
  }

  @Override

  public String dump(final IRaml api, final String newBaseUri) {
    String dump = dumpRaml();
    if (newBaseUri != null) {
      dump = replaceBaseUri(dump, newBaseUri);
    }
    return dump;
  }

  @Override
  public IRamlUpdater getRamlUpdater(final IRaml api) {
    throw new UnsupportedOperationException();
  }

  private String dumpRaml() {
    return renderApi();
  }

  @Override
  public void updateBaseUri(IRaml ignored, String baseUri) {
    if (webApi.servers() != null && webApi.servers().size() > 0) {
      final Server server = webApi.servers().get(0);
      server.withUrl(baseUri);
      server.withVariables(emptyList());
      getConsoleModel().withEncodes(webApi);
    }
  }

  // This method should only be used by API Console
  public String getAmfModel() {
    try {
      return new AmfGraphRenderer().generateString(getConsoleModel()).get();
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
      return renderer.generateString(getConsoleModel()).get();
    } catch (final InterruptedException | ExecutionException e) {
      logger.error(format("Error render API '%s' to '%s'", apiRef.getLocation(), apiVendor.name()), e);
      return "";
    }
  }

  private Document getConsoleModel() {
    if (consoleModel == null) {
      Document document = buildDocument(false);
      consoleModel = cast(new Raml10Resolver().resolve(document, "editing"));
    }

    return consoleModel;
  }

  private Document buildDocument(boolean validate) {
    final URI uri = getPathAsUri(apiRef);
    return DocumentParser.parseFile(parser, uri, validate);
  }
}
