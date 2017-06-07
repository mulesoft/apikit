/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.InvalidHeaderException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.core.exception.TypedException;
import org.mule.runtime.http.api.domain.ParameterMap;

public class HeadersValidator {

  ParameterMap headers;

  public HeadersValidator() {}

  public void validateAndAddDefaults(ParameterMap incomingHeaders, IAction action) throws TypedException
  {
    this.headers = incomingHeaders;
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

  public ParameterMap getNewHeaders() {
    return headers;
  }
}
