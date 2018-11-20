/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.mule.amf.impl.ParserWrapperAmf;
import org.mule.amf.impl.exceptions.ParserException;
import org.mule.parser.service.logger.Logger;
import org.mule.parser.service.logger.LoggerFactory;
import org.mule.raml.implv1.ParserWrapperV1;
import org.mule.raml.implv2.ParserWrapperV2;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mule.raml.interfaces.ParserType.AMF;
import static org.mule.raml.interfaces.ParserType.AUTO;
import static org.mule.raml.interfaces.ParserType.RAML;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.parser.rule.Severity.WARNING;

public class ParserService {

  public static final String MULE_APIKIT_PARSER = "mule.apikit.parser";

  private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ParserService.class);

  private Logger logger;

  public ParserService() {
    this(DEFAULT_LOGGER);
  }

  public ParserService(Logger logger) {
    this.logger = logger;
  }

  public ParserWrapper getParser(final ApiRef apiRef) {
    return getParser(apiRef, AUTO);
  }


  public ParserWrapper getParser(final ApiRef apiRef, ParserType parserType) {
    final ParserType overridden = getOverriddenParserType();
    if (overridden != AUTO)
      return getParserFor(apiRef, overridden);
    else
      return getParserFor(apiRef, parserType);
  }

  private ParserType getOverriddenParserType() {
    final String parserValue = System.getProperty(MULE_APIKIT_PARSER);
    if (AMF.name().equals(parserValue))
      return AMF;
    if (RAML.name().equals(parserValue))
      return RAML;
    return AUTO;
  }

  private ParserWrapper getParserFor(final ApiRef apiRef, ParserType parserType) {
    ParserWrapper parserWrapper;

    try {
      if (parserType == RAML) {
        parserWrapper = createRamlParserWrapper(apiRef);
      } else
        parserWrapper = ParserWrapperAmf.create(apiRef, false);

      final IValidationReport validationReport = parserWrapper.validationReport();

      if (validationReport.conforms()) {
        if (parserType == AUTO)
          parserType = AMF;
        return parserWrapper;
      } else {
        final List<IValidationResult> errorsFound = validationReport.getResults();
        return applyFallback(apiRef, parserType, errorsFound);
      }
    } catch (Exception e) {
      if (e instanceof ParserServiceException)
        throw (ParserServiceException) e;
      if (e instanceof ParserException)
        throw new ParserServiceException(e);
      final List<IValidationResult> errors = singletonList(IValidationResult.fromException(e));
      return applyFallback(apiRef, parserType, errors);
    }
  }

  // Only fallback if is RAML
  private ParserWrapper applyFallback(ApiRef apiRef, ParserType parserType, List<IValidationResult> errorsFound)
      throws ParserServiceException {
    if (parserType == AUTO) {
      final ParserWrapper fallbackParser = createRamlParserWrapper(apiRef);
      if (fallbackParser.validationReport().conforms()) {
        logErrors(errorsFound, WARNING);
        return fallbackParser;
      }
    }
    logErrors(errorsFound);
    throw new ParserServiceException(buildErrorMessage(errorsFound));
  }

  private void logErrors(List<IValidationResult> validationResults) {
    validationResults.forEach(error -> logError(error, error.getSeverity()));
  }

  private void logErrors(List<IValidationResult> validationResults, Severity overridenSeverity) {
    validationResults.forEach(error -> logError(error, overridenSeverity));
  }

  private void logError(IValidationResult error, Severity severity) {
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

  private static ParserWrapper createRamlParserWrapper(ApiRef apiRef) {
    final String path = apiRef.getLocation();
    // TODO consider whether to use v1 or v2 when vendor is raml 0.8 (ParserV2Utils.useParserV2)
    if (RAML_08.equals(apiRef.getVendor())) {
      return new ParserWrapperV1(path);
    } else {
      if (apiRef.getResourceLoader().isPresent()) {
        final org.mule.raml.interfaces.loader.ResourceLoader apiLoader = apiRef.getResourceLoader().get();
        final ResourceLoader resourceLoader =
            new CompositeResourceLoader(new DefaultResourceLoader(), apiLoader::getResourceAsStream);
        return new ParserWrapperV2(path, resourceLoader);
      }
      return new ParserWrapperV2(path);
    }
  }
}
