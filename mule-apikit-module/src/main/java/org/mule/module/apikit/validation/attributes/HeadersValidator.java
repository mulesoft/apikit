/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.net.MediaType;
import org.mule.module.apikit.HeaderName;
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

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;


public class HeadersValidator {


  private static final Logger logger = LoggerFactory.getLogger(HeadersValidator.class);

  private MultiMap<String, String> headers;

  public HeadersValidator() {}

  public void validateAndAddDefaults(MultiMap<String, String> incomingHeaders, IAction action, boolean headersStrictValidation)
      throws InvalidHeaderException, NotAcceptableException {
    this.headers = incomingHeaders;
    analyseRequestHeaders(action, headersStrictValidation);
    analyseAcceptHeader(incomingHeaders, action);
  }

  private void analyseRequestHeaders(IAction action, boolean headersStrictValidation) throws InvalidHeaderException {
    if (headersStrictValidation)
      validateHeadersStrictly(action);

    for (Map.Entry<String, IParameter> entry : action.getHeaders().entrySet()) {
      final String ramlHeader = entry.getKey();
      final IParameter ramlType = entry.getValue();

      if (ramlHeader.contains("{?}")) {
        final String regex = ramlHeader.replace("{?}", ".*");
        for (String incomingHeader : headers.keySet()) {
          if (incomingHeader.matches(regex))
            validateHeader(headers.getAll(incomingHeader), ramlHeader, ramlType);
        }
      } else {
        final List<String> values = AttributesHelper.getParamsIgnoreCase(headers, ramlHeader);
        if (values.isEmpty() && ramlType.isRequired()) {
          throw new InvalidHeaderException("Required header " + ramlHeader + " not specified");
        }
        if (values.isEmpty() && ramlType.getDefaultValue() != null) {
          headers = AttributesHelper.addParam(headers, ramlHeader, ramlType.getDefaultValue());
        }
        validateHeader(values, ramlHeader, ramlType);
      }
    }
  }

  private void validateHeadersStrictly(IAction action) throws InvalidHeaderException {
    //checks that headers are defined in the RAML
    final Set<String> ramlHeaders = action.getHeaders().keySet().stream()
        .map(String::toLowerCase)
        .collect(toSet());

    final Set<String> templateHeaders = ramlHeaders.stream()
        .filter(header -> header.contains("{?}"))
        .map(header -> header.replace("{?}", ".*"))
        .collect(toSet());

    final Set<String> unmatchedHeaders = headers.keySet().stream()
        .filter(header -> templateHeaders.stream().noneMatch(header::matches))
        .collect(toSet());

    final Set<String> standardHeaders = stream(HeaderName.values())
        .map(header -> header.getName().toLowerCase())
        .collect(toSet());

    final Set<String> undefinedHeaders = difference(unmatchedHeaders, union(ramlHeaders, standardHeaders));

    if (!undefinedHeaders.isEmpty()) {
      throw new InvalidHeaderException(on(", ").join(undefinedHeaders)
          + " headers are not defined in RAML and strict headers validation property is true.");
    }
  }

  private void validateHeader(List<String> values, String name, IParameter type)
      throws InvalidHeaderException {
    if (values.isEmpty())
      return;

    if (values.size() > 1 && !type.isArray() && !type.isRepeat())
      throw new InvalidHeaderException("Header " + name + " is not repeatable");

    // raml 1.0 array validation
    if (type.isArray()) {
      validateType(name, values, type);
    } else {
      // single header or repeat
      validateType(name, values.get(0), type);
    }
  }

  private void validateType(String name, List<String> values, IParameter type) throws InvalidHeaderException {
    final StringBuilder yamlValue = new StringBuilder();
    for (String value : values)
      yamlValue.append("- ").append(value).append("\n");

    validateType(name, yamlValue.toString(), type);
  }

  private void validateType(String name, String value, IParameter type) throws InvalidHeaderException {
    if (!type.validate(value)) {
      throw new InvalidHeaderException(format("Invalid value '%s' for header %s. %s", value, name, type.message(value)));
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
        logger.debug(format("=== adding response mimeTypes for status %d : %s", status, mimeTypes));
      }
    }
    return mimeTypes;
  }

  protected int getSuccessStatus(IAction action) {
    for (String status : action.getResponses().keySet()) {
      if ("default".equalsIgnoreCase(status))
        break;

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
