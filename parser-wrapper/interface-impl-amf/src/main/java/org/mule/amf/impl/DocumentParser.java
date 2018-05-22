/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileNames;
import amf.client.AMF;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Oas20Parser;
import amf.client.parse.Parser;
import amf.client.parse.RamlParser;
import amf.client.render.AmfGraphRenderer;
import amf.client.render.Oas20Renderer;
import amf.client.render.Raml10Renderer;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.plugins.features.validation.AMFValidatorPlugin;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.mule.amf.impl.exceptions.ParserException;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class DocumentParser {

  private DocumentParser() {}

  private static RamlParser ramlParser() {
    return AMF.ramlParser();
  }

  private static Oas20Parser oas20Parser() {
    return AMF.oas20Parser();
  }

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

  private static Document parseFile(final Parser parser, final String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  public static WebApi getWebApi(final File file) throws ParserException {
    return getWebApi(getParserForApi(file), file.toPath());
  }

  private static Parser getParserForApi(final File apiDefinition) {
    final String ext = getExtension(apiDefinition.getAbsolutePath());
    return "raml".equalsIgnoreCase(ext) || "yaml".equalsIgnoreCase(ext) || "yml".equalsIgnoreCase(ext) ? ramlParser()
        : oas20Parser();
  }

  private static WebApi getWebApi(final Parser parser, final Path path) throws ParserException {
    return getWebApi(parseFile(parser, path.toUri().toString()));
  }

  private static WebApi getWebApi(final Document document) throws ParserException {
    return (WebApi) document.encodes();
  }

  private static ValidationReport getParsingReport(final Oas20Parser parser) throws ParserException {
    return getParsingReport(parser, ProfileNames.OAS());
  }

  private static ValidationReport getParsingReport(final Parser parser, final String profile) throws ParserException {
    return handleFuture(parser.reportValidation(profile));
  }

  static {
    try {
      AMF.init().get();
      AMFValidatorPlugin.withEnabledValidation(true);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
