/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.collect.Maps;
import org.mule.module.apikit.api.exception.InvalidQueryStringException;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IQueryString;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class QueryStringValidator {

  private IAction action;

  public QueryStringValidator(IAction action) {
    this.action = action;
  }

  public void validate(MultiMap<String, String> queryParams) throws InvalidQueryStringException {
    IQueryString expected = action.queryString();
    if (!shouldProcessQueryString(expected))
      return;

    String queryString = buildQueryString(expected, queryParams);

    if (!expected.validate(queryString)) {
      throw new InvalidQueryStringException("Invalid value for query string ");
    }
  }

  private boolean shouldProcessQueryString(IQueryString queryString) {
    return queryString != null && !queryString.isArray() && !queryString.isScalar();
  }

  private String buildQueryString(IQueryString expected, MultiMap<String, String> queryParams) {
    StringBuilder result = new StringBuilder();

    Map<String, IParameter> facetsWithDefault = getFacetsWithDefaultValue(expected.facets());

    for (Object property : queryParams.keySet()) {
      facetsWithDefault.remove(property.toString());
      final List<String> actualQueryParam = queryParams.getAll(property.toString());

      result.append("\n").append(property).append(": ");

      if (actualQueryParam.size() > 1 || expected.isFacetArray(property.toString())) {
        for (Object o : actualQueryParam) {
          result.append("\n  - ").append(o);
        }
        result.append("\n");
      } else {
        for (Object o : actualQueryParam) {
          result.append(o).append("\n");
        }
      }
    }

    for (Entry<String, IParameter> entry : facetsWithDefault.entrySet())
      result.append(entry.getKey()).append(": ").append(entry.getValue().getDefaultValue()).append("\n");

    if (result.length() > 0)
      return result.toString();
    if (expected.getDefaultValue() != null)
      return expected.getDefaultValue();

    return "{}";
  }

  private Map<String, IParameter> getFacetsWithDefaultValue(Map<String, IParameter> facets) {
    HashMap<String, IParameter> result = Maps.newHashMap();
    for (Entry<String, IParameter> entry : facets.entrySet()) {
      if (entry.getValue().getDefaultValue() != null)
        result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
