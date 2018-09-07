/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.module.apikit.api.Parser;
import org.mule.module.apikit.exception.ParserInitializationException;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mule.module.apikit.api.Parser.AMF;
import static org.mule.module.apikit.api.Parser.AUTO;
import static org.mule.module.apikit.api.Parser.RAML;
import static org.mule.raml.interfaces.parser.rule.Severity.WARNING;

public class ParserService {

  private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

  private final String ramlPath;
  private ResourceLoader resourceLoaderV2;
  private ParserWrapper parserWrapper;

  private Parser parser;

  private static final String RAML_V1 = "RAML_V1";
  private static final String RAML_V2 = "RAML_V2";
  private String ramlParserVersion;

  public ParserService(String ramlPath, Parser parser) {
    this.ramlPath = ramlPath;
    this.parser = parser;
    resourceLoaderV2 = new DefaultResourceLoader();
    parserWrapper = initParserWrapper(ramlPath);
  }

  /**
   * @deprecated use getParser() and getApiVendor() instead.
   */
  @Deprecated
  public boolean isParserV2() {
    return parser == AMF || (parser == RAML && RAML_V2.equals(ramlParserVersion));
  }

  public Parser getParser() {
    return parser;
  }

  public ApiVendor getApiVendor() {
    return parserWrapper.getApiVendor();
  }

  private String getRamlParserVersion() {
    InputStream content = resourceLoaderV2.fetchResource(ramlPath);

    if (content == null)
      throw new RuntimeException("Couldn't find file '" + ramlPath + "'");

    String dump = StreamUtils.toString(content);
    return ParserV2Utils.useParserV2(dump) ? RAML_V2 : RAML_V1;
  }

  private ParserWrapper initParserWrapper(String ramlPath) throws ParserInitializationException {
    ParserWrapper parserWrapper;

    try {
      if (parser == RAML)
        parserWrapper = initRamlWrapper(ramlPath);
      else
        parserWrapper = ParserWrapperAmf.create(getPathAsUri(ramlPath), false);

      final IValidationReport validationReport = parserWrapper.validationReport();

      if (validationReport.conforms()) {
        if (parser == AUTO)
          parser = AMF;
        return parserWrapper;
      } else {
        final List<IValidationResult> errorsFound = validationReport.getResults();
        return applyFallback(ramlPath, errorsFound);
      }
    } catch (Exception e) {
      if (e instanceof ParserInitializationException)
        throw (ParserInitializationException) e;
      final List<IValidationResult> errors = singletonList(IValidationResult.fromException(e));
      return applyFallback(ramlPath, errors);
    }
  }

  private ParserWrapper applyFallback(String ramlPath, List<IValidationResult> errorsFound) throws ParserInitializationException {
    if (parser == AUTO) {
      parser = RAML;
      final ParserWrapper fallbackParser = initRamlWrapper(ramlPath);
      if (fallbackParser.validationReport().conforms()) {
        logErrors(errorsFound, WARNING);
        return fallbackParser;
      } else {
        logErrors(errorsFound);
        throw new ParserInitializationException(buildErrorMessage(errorsFound));
      }
    } else {
      logErrors(errorsFound);
      throw new ParserInitializationException(buildErrorMessage(errorsFound));
    }
  }

  private static void logErrors(List<IValidationResult> validationResults) {
    validationResults.stream().forEach(error -> logError(error, error.getSeverity()));
  }

  private static void logErrors(List<IValidationResult> validationResults, Severity overridenSeverity) {
    validationResults.stream().forEach(error -> logError(error, overridenSeverity));
  }

  private static void logError(IValidationResult error, Severity severity) {
    if (severity == Severity.INFO)
      logger.info(error.getMessage());
    else if (severity == WARNING)
      logger.warn(error.getMessage());
    else
      logger.error(error.getMessage());
  }

  private static String buildErrorMessage(List<IValidationResult> validationResults) {
    final StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
    message.append(validationResults.size()).append("\n\n");
    for (IValidationResult error : validationResults) {
      message.append(error.getMessage()).append("\n");
    }
    return message.toString();
  }

  private ParserWrapper initRamlWrapper(String ramlPath) {
    ParserWrapper parserWrapper;
    ramlParserVersion = getRamlParserVersion();
    if (RAML_V1.equals(ramlParserVersion))
      parserWrapper = new ParserWrapperV1(ramlPath);
    else
      parserWrapper = new ParserWrapperV2(ramlPath);
    return parserWrapper;
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
