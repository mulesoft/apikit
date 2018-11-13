/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.google.common.base.Joiner;
import org.mule.module.apikit.api.exception.InvalidQueryParameterException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.api.util.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.difference;

public class QueryParameterValidator {

  private MultiMap<String, String> queryParams;
  private String queryString;
  private IAction action;

  public QueryParameterValidator(IAction action) {
    this.action = action;
  }

  public void validateAndAddDefaults(MultiMap<String, String> queryParams, String queryString,
                                     boolean queryParamsStrictValidation)
      throws InvalidQueryParameterException {

    if (queryParamsStrictValidation)
      validateQueryParametersStrictly(queryParams);

    for (String expectedKey : action.getQueryParameters().keySet()) {
      IParameter expected = action.getQueryParameters().get(expectedKey);
      List<String> actual = queryParams.getAll(expectedKey);

      if (actual.isEmpty()) {
        if (expected.isRequired()) {
          throw new InvalidQueryParameterException("Required query parameter " + expectedKey + " not specified");
        }

        if (expected.getDefaultValue() != null) {
          queryString =
              AttributesHelper.addQueryString(queryString, queryParams.size(), expectedKey, expected.getDefaultValue());

          queryParams = AttributesHelper.addParam(queryParams, expectedKey, expected.getDefaultValue());
        }
      } else {

        if (actual.size() > 1 && !(expected.isRepeat() || expected.isArray())) {
          throw new InvalidQueryParameterException("Query parameter " + expectedKey + " is not repeatable");
        }

        if (expected.isArray()) {
          // raml 1.0 array validation
          validateQueryParamArray(expectedKey, expected, actual);
        } else {
          // single query param or repeat
          //noinspection unchecked
          for (String param : actual) {
            validateQueryParam(expectedKey, expected, param);
          }
        }
      }
    }

    this.queryParams = queryParams;
    this.queryString = queryString;
  }

  private void validateQueryParametersStrictly(MultiMap<String, String> queryParams) throws InvalidQueryParameterException {
    //check that query parameters are defined in the RAML
    final Set notDefinedQueryParameters = difference(queryParams.keySet(), action.getQueryParameters().keySet());
    if (!notDefinedQueryParameters.isEmpty())
      throw new InvalidQueryParameterException(Joiner.on(", ").join(notDefinedQueryParameters)
          + " parameters are not defined in RAML.");
  }

  public MultiMap<String, String> getQueryParams() {
    return queryParams;
  }

  public String getQueryString() {
    return queryString;
  }

  //only for raml 1.0
  private void validateQueryParamArray(String paramKey, IParameter expected, Collection<?> paramValues)
      throws InvalidQueryParameterException {
    StringBuilder builder = new StringBuilder();

    paramValues.forEach(paramValue -> {
      final String value = String.valueOf(paramValue);
      builder.append("- ");
      builder.append(expected.surroundWithQuotesIfNeeded(value));
      builder.append("\n");
    });

    validateQueryParam(paramKey, expected, builder.toString());
  }

  private void validateQueryParam(String paramKey, IParameter expected, String paramValue) throws InvalidQueryParameterException {
    if (!expected.validate(paramValue)) {
      String msg = String.format("Invalid value '%s' for query parameter %s. %s",
                                 paramValue, paramKey, expected.message(paramValue));

      throw new InvalidQueryParameterException(msg);
    }
  }

}
