/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.core.exception.TypedException;
import org.mule.runtime.http.api.domain.ParameterMap;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadersValidator {


  private static final Logger logger = LoggerFactory.getLogger(HeadersValidator.class);

  private ParameterMap headers;

  public HeadersValidator() {}

  public void validateAndAddDefaults(ParameterMap incomingHeaders, IAction action) throws TypedException
  {
    this.headers = incomingHeaders;
    analyseRequestHeaders(action);
    analyseAcceptHeader(incomingHeaders, action);
  }

  private void analyseRequestHeaders(IAction action)
  {
    for (String expectedKey : action.getHeaders().keySet()) {
      IParameter expected = action.getHeaders().get(expectedKey);

      if (expectedKey.contains("{?}")) {
        String regex = expectedKey.replace("{?}", ".*");
        for (String incoming : headers.keySet()) {
          String incomingValue = AttributesHelper.getParamIgnoreCase(headers, incoming);
          if (incoming.matches(regex) && !expected.validate(incomingValue)) {
            String msg = String.format("Invalid value '%s' for header %s. %s",
                                       incomingValue, expectedKey, expected.message(incomingValue));
            throw ApikitErrorTypes.throwErrorTypeNew(new InvalidHeaderException(msg));
          }
        }
      } else {
        String actual = AttributesHelper.getParamIgnoreCase(headers, expectedKey);
        if (actual == null && expected.isRequired()) {
          throw ApikitErrorTypes.throwErrorTypeNew(new InvalidHeaderException("Required header " + expectedKey + " not specified"));
        }
        if (actual == null && expected.getDefaultValue() != null) {
          headers = AttributesHelper.addParam(headers, expectedKey, expected.getDefaultValue());
        }
        if (actual != null) {
          if (!expected.validate(actual)) {
            String msg = String.format("Invalid value '%s' for header %s. %s",
                                       actual, expectedKey, expected.message(actual));
            throw ApikitErrorTypes.throwErrorTypeNew(new InvalidHeaderException(msg));
          }
        }
      }
    }
  }

  private void analyseAcceptHeader(ParameterMap incomingHeaders, IAction action)
  {
    List<String> mimeTypes = getResponseMimeTypes(action);
    if (action == null || action.getResponses() == null || mimeTypes.isEmpty())
    {
      //no response media-types defined, return no body
      return;
    }
    MediaType bestMatch = MimeTypeParser.bestMatch(mimeTypes, AttributesHelper.getAcceptedResponseMediaTypes(incomingHeaders));
    if (bestMatch == null)
    {
      throw ApikitErrorTypes.throwErrorTypeNew(new NotAcceptableException());
    }
    logger.debug("=== negotiated response content-type: " + bestMatch.toString());
    for (String representation : mimeTypes)
    {
      if (representation.equals(bestMatch.withoutParameters().toString()))
      {
        //there is a valid representation
        return;
      }
    }
    throw ApikitErrorTypes.throwErrorTypeNew(new NotAcceptableException());
  }

  private List<String> getResponseMimeTypes(IAction action)
  {
    List<String> mimeTypes = new ArrayList<>();
    int status = getSuccessStatus(action);
    if (status != -1)
    {
      IResponse response = action.getResponses().get(String.valueOf(status));
      if (response != null && response.hasBody())
      {
        Map<String, IMimeType> interfacesOfTypes = response.getBody();
        for (Map.Entry<String, IMimeType> entry : interfacesOfTypes.entrySet())
        {
          mimeTypes.add(entry.getValue().getType());
        }
        logger.debug(String.format("=== adding response mimeTypes for status %d : %s", status, mimeTypes));
      }
    }
    return mimeTypes;
  }

  protected int getSuccessStatus(IAction action)
  {
    for (String status : action.getResponses().keySet())
    {
      int code = Integer.parseInt(status);
      if (code >= 200 && code < 300)
      {
        return code;
      }
    }
    //default success status
    return 200;
  }

  public ParameterMap getNewHeaders() {
    return headers;
  }
}
