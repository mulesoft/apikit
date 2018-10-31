/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.ApiRef;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;
import static org.mule.raml.interfaces.ParserType.AMF;
import static org.mule.raml.interfaces.ParserType.AUTO;
import static org.mule.raml.interfaces.ParserType.RAML;
import static org.mule.raml.interfaces.parser.rule.Severity.WARNING;

public class ParserService {

  private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

  private static final String RAML_V1 = "RAML_V1";
  private static final String RAML_V2 = "RAML_V2";
  private static final String HEADER_RAML_10 = "#%RAML 1.0";
  private static final String HEADER_RAML_08 = "#%RAML 0.8";


  public static ParserWrapper create(final ApiRef apiRef, ParserType parserType) {
    return createParserWrapper(apiRef.getLocation(), parserType);
  }

  private static ParserWrapper createParserWrapper(final String path, ParserType parserType) throws ParserServiceException {
    ParserWrapper parserWrapper;

    try {
      if (parserType == RAML) {
        parserWrapper = createRamlParserWrapper(path, getRamlVersion(path).get());
      } else
        parserWrapper = ParserWrapperAmf.create(getPathAsUri(path), false);

      final IValidationReport validationReport = parserWrapper.validationReport();

      if (validationReport.conforms()) {
        if (parserType == AUTO)
          parserType = AMF;
        return parserWrapper;
      } else {
        final List<IValidationResult> errorsFound = validationReport.getResults();
        return applyFallback(path, parserType, errorsFound);
      }
    } catch (Exception e) {
      if (e instanceof ParserServiceException)
        throw (ParserServiceException) e;
      final List<IValidationResult> errors = singletonList(IValidationResult.fromException(e));
      return applyFallback(path, parserType, errors);
    }
  }

  // Only fallback if is RAML
  private static ParserWrapper applyFallback(String path, ParserType parserType, List<IValidationResult> errorsFound)
      throws ParserServiceException {
    if (parserType == AUTO) {
      final Optional<String> ramlVersion = getRamlVersion(path);
      if (ramlVersion.isPresent()) {
        final ParserWrapper fallbackParser = createRamlParserWrapper(path, ramlVersion.get());
        if (fallbackParser.validationReport().conforms()) {
          logErrors(errorsFound, WARNING);
          return fallbackParser;
        }
      }
    }
    logErrors(errorsFound);
    throw new ParserServiceException(buildErrorMessage(errorsFound));
  }

  private static void logErrors(List<IValidationResult> validationResults) {
    validationResults.forEach(error -> logError(error, error.getSeverity()));
  }

  private static void logErrors(List<IValidationResult> validationResults, Severity overridenSeverity) {
    validationResults.forEach(error -> logError(error, overridenSeverity));
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

  private static ParserWrapper createRamlParserWrapper(final String path, final String ramlVersion) {
    return RAML_V1.equals(ramlVersion) ? new ParserWrapperV1(path) : new ParserWrapperV2(path);
  }

  private static Optional<String> getRamlVersion(final String path) {
    final ResourceLoader resourceLoaderV2 = new DefaultResourceLoader();
    final InputStream inputStream = resourceLoaderV2.fetchResource(path);
    if (inputStream == null)
      throw new RuntimeException("Couldn't find file '" + path + "'");

    final String content = StreamUtils.toString(inputStream);
    return content.startsWith(HEADER_RAML_10) ? Optional.of(RAML_V2)
        : content.startsWith(HEADER_RAML_08) ? Optional.of(RAML_V1) : Optional.empty();
  }

  private static URI getPathAsUri(String path) {
    try {
      final URI uri = new URI(path);
      return uri.isAbsolute() ? uri : getUriFromFile(path);
    } catch (URISyntaxException e) {
      return getUriFromFile(path);
    }
  }

  // It means that it's a file
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
