/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import amf.ProfileName;
import amf.ProfileNames;
import amf.client.AMF;
import amf.client.environment.Environment;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.model.domain.WebApi;
import amf.client.parse.Oas20Parser;
import amf.client.parse.Oas20YamlParser;
import amf.client.parse.Parser;
import amf.client.parse.Raml08Parser;
import amf.client.parse.Raml10Parser;
import amf.client.parse.RamlParser;
import amf.client.resolve.Raml10Resolver;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import amf.plugins.features.validation.AMFValidatorPlugin;
import amf.plugins.xml.XmlValidationPlugin;
import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.sun.jmx.mbeanserver.Util.cast;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class DocumentParser {

  private static final Logger logger = LoggerFactory.getLogger(DocumentParser.class);

  private DocumentParser() {}

  private static <T, U> U handleFuture(CompletableFuture<T> f) throws ParserException {
    try {
      return (U) f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException("An error happened while parsing the api. Message: " + e.getMessage(), e);
    }
  }

  public static Document parseFile(final Parser parser, final URI uri) throws ParserException {
    return parseFile(parser, uri, false);
  }

  public static Document parseFile(final Parser parser, final URI uri, final boolean validate) throws ParserException {
    return parseFile(parser, uriToPath(uri), validate);
  }

  public static Document parseFile(final Parser parser, final String apiPath, final boolean validate)
      throws ParserException {
    final Document document = parseFile(parser, apiPath);

    if (validate) {
      final ProfileName profile = parser instanceof Oas20Parser ? ProfileNames.OAS() : ProfileNames.RAML();
      final ValidationReport parsingReport = DocumentParser.getParsingReport(parser, profile);
      if (!parsingReport.conforms()) {
        final List<ValidationResult> results = parsingReport.results();
        if (!results.isEmpty()) {
          final String message = results.get(0).message();
          throw new ParserException(message);
        }
      }
    }
    return document;
  }

  private static String uriToPath(final URI uri) {
    final String path = uri.toString();
    return URLDecoder.decode(path);
  }

  private static Document parseFile(final Parser parser, final String url) throws ParserException {
    return handleFuture(parser.parseFileAsync(url));
  }

  public static Parser getParserForApi(final ApiRef apiRef, Environment environment) {
    final ApiVendor vendor = apiRef.getVendor();

    switch (vendor) {
      case OAS:
      case OAS_20:
        if ("JSON".equalsIgnoreCase(apiRef.getFormat()))
          return new Oas20Parser(environment);
        else
          return new Oas20YamlParser(environment);
      case RAML_08:
        return new Raml08Parser(environment);
      case RAML_10:
        return new Raml10Parser(environment);
      default:
        return new RamlParser(environment);
    }
  }

  public static WebApi getWebApi(final Parser parser, final URI uri) throws ParserException {
    return getWebApi(parseFile(parser, uri));
  }

  public static WebApi getWebApi(final BaseUnit baseUnit) throws ParserException {
    final Document document = cast(AMF.resolveRaml10(baseUnit));
    return cast(document.encodes());
  }

  public static ValidationReport getParsingReport(final Parser parser, final ProfileName profile) throws ParserException {
    return handleFuture(parser.reportValidation(profile));
  }

  public static VendorEx getVendor(final URI api) {
    final String ext = getExtension(api.getPath());
    return "RAML".equalsIgnoreCase(ext) ? VendorEx.RAML : deduceVendorFromContent(api);
  }

  private static VendorEx deduceVendorFromContent(final URI api) {

    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(api.toURL().openStream()));

      final String firstLine = getFirstLine(in);

      if (firstLine.toUpperCase().contains("#%RAML"))
        return VendorEx.RAML;

      final boolean isJson = firstLine.startsWith("{") || firstLine.startsWith("[");
      // Some times swagger version is in the first line too, e.g. yaml files
      if (firstLine.toUpperCase().contains("SWAGGER"))
        return isJson ? VendorEx.OAS20_JSON : VendorEx.OAS20_YAML;

      int lines = 0;
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        if (inputLine.toUpperCase().contains("SWAGGER"))
          return isJson ? VendorEx.OAS20_JSON : VendorEx.OAS20_YAML;
        if (++lines == 10)
          break;
      }
    } catch (final Exception ignore) {
    } finally {
      IOUtils.closeQuietly(in);
    }

    return VendorEx.RAML; // default value
  }

  private static String getFirstLine(BufferedReader in) throws IOException {
    String line;
    while ((line = in.readLine()) != null) {
      if (line.trim().length() > 0)
        return line;
    }
    return "";
  }

  static {
    try {
      AMF.init().get();
      AMFValidatorPlugin.withEnabledValidation(true);
      amf.core.AMF.registerPlugin(new XmlValidationPlugin());
    } catch (final Exception e) {
      logger.error("Error initializing AMF", e);
    }
  }

  public enum VendorEx {
    RAML,
    OAS20_JSON,
    OAS20_YAML
  }
}
