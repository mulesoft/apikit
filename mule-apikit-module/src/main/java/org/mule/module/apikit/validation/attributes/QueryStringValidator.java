/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.mule.module.apikit.api.exception.InvalidQueryStringException;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.util.MultiMap;

import java.util.List;

public class QueryStringValidator {

  private IAction action;

  public QueryStringValidator(IAction action) {
    this.action = action;
  }

  public void validate(MultiMap<String, String> queryParams) throws InvalidQueryStringException {
    IParameter expected = action.getQueryString();
    if (!shouldProcessQueryString(expected))
      return;

    String queryString = buildQueryString(expected, queryParams);

    if (!expected.validate(queryString)) {
      throw new InvalidQueryStringException("Invalid value for query string ");
    }
  }

  private boolean shouldProcessQueryString(IParameter queryString) {
    return queryString != null && !queryString.isArray() && !queryString.isScalar();
  }

  private String buildQueryString(IParameter expected, MultiMap<String, String> queryParams) {
    StringBuilder result = new StringBuilder();

    for (Object property : queryParams.keySet()) {

      final List<String> actualQueryParam = queryParams.getAll(property.toString());

      result.append("\n").append(property).append(": ");

      if (actualQueryParam.size() > 1 || expected.isFacetArray(property.toString())) {
        for (Object o : actualQueryParam) {
          result.append("\n  - ").append(o);
        }
      } else {
        for (Object o : actualQueryParam)
          result.append(o).append("\n");
      }
    }
    // if empty validate return an empty json
    return result.length() == 0 ? "{}" : result.toString();
  }
}
