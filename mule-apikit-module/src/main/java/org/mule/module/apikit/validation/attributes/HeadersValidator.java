/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.base.Joiner;
import com.google.common.net.MediaType;
import org.mule.module.apikit.HeaderNames;
import org.mule.module.apikit.api.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.*;

public class HeadersValidator {


  private static final Logger logger = LoggerFactory.getLogger(HeadersValidator.class);

  private MultiMap<String, String> headers;

  public HeadersValidator() {}

  public void validateAndAddDefaults(MultiMap<String, String> incomingHeaders, IAction action, boolean isMuleThreeCompatibility)
      throws InvalidHeaderException, NotAcceptableException {
    this.headers = incomingHeaders;
    analyseRequestHeaders(action, isMuleThreeCompatibility);
    analyseAcceptHeader(incomingHeaders, action);
  }

  private void analyseRequestHeaders(IAction action, boolean isMuleThreeCompatibility) throws InvalidHeaderException {
    if (!isMuleThreeCompatibility)
      validateHeadersStrictly(action);

    for (String expectedKey : action.getHeaders().keySet()) {
      IParameter expected = action.getHeaders().get(expectedKey);

      if (expectedKey.contains("{?}")) {
        String regex = expectedKey.replace("{?}", ".*");
        for (String incoming : headers.keySet()) {
          if (incoming.matches(regex))
            validateHeader(headers.getAll(incoming), expectedKey, expected, isMuleThreeCompatibility);
        }
      } else {
        List<String> actual = AttributesHelper.getParamsIgnoreCase(headers, expectedKey);
        if (actual.isEmpty() && expected.isRequired()) {
          throw new InvalidHeaderException("Required header " + expectedKey + " not specified");
        }
        if (actual.isEmpty() && expected.getDefaultValue() != null) {
          headers = AttributesHelper.addParam(headers, expectedKey, expected.getDefaultValue());
        }
        validateHeader(actual, expectedKey, expected, isMuleThreeCompatibility);
      }
    }
  }

  private void validateHeadersStrictly(IAction action) throws InvalidHeaderException {
    //checks that headers are defined in the RAML
    final Set<String> headersDefinedInRAML =
        action.getHeaders().keySet().stream().map(String::toLowerCase).collect(Collectors.toSet());

    final Set<String> standardHttpHeaders = newHashSet(HeaderNames.values()).stream()
        .map(header -> header.getName().toLowerCase()).collect(Collectors.toSet());

    final Set<String> headersWithPlaceholder = headersDefinedInRAML.stream().filter(header -> header.contains("{?}"))
        .map(header -> header.replace("{?}", ".*")).collect(Collectors.toSet());
    final Predicate<String> noRegexMatch = header -> headersWithPlaceholder.stream().noneMatch(header::matches);
    final Set<String> placeholderHeadersRemoved = filter(headers.keySet(), noRegexMatch::test);

    final Set<String> notDefinedHeaders = difference(placeholderHeadersRemoved, union(headersDefinedInRAML, standardHttpHeaders));
    if (!notDefinedHeaders.isEmpty())
      throw new InvalidHeaderException(Joiner.on(", ").join(notDefinedHeaders) + " headers are not defined in RAML.");
  }

  private void validateHeader(List<String> headerValues, String expectedKey, IParameter expectedValue,
                              boolean isMuleThreeCompatibility)
      throws InvalidHeaderException {
    if (headerValues.isEmpty())
      return;

    if (!isMuleThreeCompatibility && headerValues.size() > 1 && !expectedValue.isArray() && !expectedValue.isRepeat())
      throw new InvalidHeaderException("Header " + expectedKey + " is not repeatable");

    // raml 1.0 array validation
    if (expectedValue.isArray()) {
      validateHeaderArray(headerValues, expectedKey, expectedValue);
    } else {
      // single header or repeat
      for (String value : headerValues)
        validateHeader(expectedKey, expectedValue, value);
    }
  }

  private void validateHeaderArray(List<String> headerValues, String expectedKey, IParameter expectedValue)
      throws InvalidHeaderException {
    StringBuilder builder = new StringBuilder();
    for (String value : headerValues)
      builder.append("- ").append(value).append("\n");

    validateHeader(expectedKey, expectedValue, builder.toString());
  }

  private void validateHeader(String paramKey, IParameter expected, String headerValue) throws InvalidHeaderException {
    if (!expected.validate(headerValue)) {
      String msg = String.format("Invalid value '%s' for header %s. %s",
                                 headerValue, paramKey, expected.message(headerValue));

      throw new InvalidHeaderException(msg);
    }
  }

  private void analyseAcceptHeader(MultiMap<String, String> incomingHeaders, IAction action) throws NotAcceptableException {
    List<String> mimeTypes = getResponseMimeTypes(action);
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty()) {
      //no response media-types defined, return no body
      return;
    }
    MediaType bestMatch = MimeTypeParser.bestMatch(mimeTypes, AttributesHelper.getAcceptedResponseMediaTypes(incomingHeaders));
    if (bestMatch == null) {
      throw new NotAcceptableException();
    }
    logger.debug("=== negotiated response content-type: " + bestMatch.toString());
    for (String representation : mimeTypes) {
      if (representation.equals(bestMatch.withoutParameters().toString())) {
        //there is a valid representation
        return;
      }
    }
    throw new NotAcceptableException();
  }

  private List<String> getResponseMimeTypes(IAction action) {
    List<String> mimeTypes = new ArrayList<>();
    int status = getSuccessStatus(action);
    if (status != -1) {
      IResponse response = action.getResponses().get(String.valueOf(status));
      if (response != null && response.hasBody()) {
        Map<String, IMimeType> interfacesOfTypes = response.getBody();
        for (Map.Entry<String, IMimeType> entry : interfacesOfTypes.entrySet()) {
          mimeTypes.add(entry.getValue().getType());
        }
        logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, mimeTypes));
      }
    }
    return mimeTypes;
  }

  protected int getSuccessStatus(IAction action) {
    for (String status : action.getResponses().keySet()) {
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300) {
        return code;
      }
    }
    //default success status
    return 200;
  }

  public MultiMap<String, String> getNewHeaders() {
    return headers;
  }
}
