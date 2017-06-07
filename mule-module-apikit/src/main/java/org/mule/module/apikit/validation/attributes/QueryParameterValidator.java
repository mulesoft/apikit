/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import java.util.Collection;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.runtime.core.exception.TypedException;
import org.mule.runtime.http.api.domain.ParameterMap;

public class QueryParameterValidator {

  ParameterMap queryParams;
  String queryString;
  IAction action;

  public QueryParameterValidator(IAction action) {
    this.action = action;

  }

  public void validateAndAddDefaults(ParameterMap queryParams, String queryString) throws TypedException
  {

    for (String expectedKey : action.getQueryParameters().keySet()) {
      IParameter expected = action.getQueryParameters().get(expectedKey);
      Collection<?> actual = queryParams.getAll(expectedKey);

      if (actual.isEmpty()) {
        if (expected.isRequired()) {
          throw ApikitErrorTypes.throwErrorTypeNew(new InvalidQueryParameterException("Required query parameter " + expectedKey + " not specified"));
        }

        if (expected.getDefaultValue() != null) {
          queryString =
              AttributesHelper.addQueryString(queryString, queryParams.size(), expectedKey, expected.getDefaultValue());

          queryParams = AttributesHelper.addParam(queryParams, expectedKey, expected.getDefaultValue());
        }
      } else {

        if (actual.size() > 1 && !(expected.isRepeat() || expected.isArray())) {
          throw ApikitErrorTypes.throwErrorTypeNew(new InvalidQueryParameterException("Query parameter " + expectedKey + " is not repeatable"));
        }

        if (expected.isArray()) {
          // raml 1.0 array validation
          validateQueryParamArray(expectedKey, expected, actual);
        } else {
          // single query param or repeat
          //noinspection unchecked
          for (String param : (Collection<String>) actual) {
            validateQueryParam(expectedKey, expected, param);
          }
        }
      }
    }

    this.queryParams = queryParams;
    this.queryString = queryString;
  }

  public ParameterMap getQueryParams() {
    return queryParams;
  }

  public String getQueryString() {
    return queryString;
  }

  //private Collection<?> getActualQueryParam(String expectedKey)
  //{
  //    //Object queryParamsMap = ((HttpRequestAttributes)requestEvent.getMessage().getAttributes()).getQueryParams(); // requestEvent.getMessage().getInboundProperty("http.query.params");
  //    Collection<?> actual;
  //    actual = Collections.emptyList();
  //    if (queryParamsMap instanceof ParameterMap)
  //    {
  //        actual = ((ParameterMap) queryParamsMap).getAll(expectedKey);
  //    }
  //    else
  //    {
  //        Object param = ((Map) queryParamsMap).get(expectedKey);
  //        if (param instanceof Collection)
  //        {
  //            actual = (Collection<?>) param;
  //        }
  //        else if (param != null)
  //        {
  //            actual = ImmutableList.of(param);
  //        }
  //    }
  //    return actual;
  //}

  //only for raml 1.0
  private void validateQueryParamArray(String paramKey, IParameter expected, Collection<?> paramValue)
          throws TypedException
  {
    StringBuilder builder = new StringBuilder();
    for (Object item : paramValue) {
      builder.append("- ").append(String.valueOf(item)).append("\n");
    }
    validateQueryParam(paramKey, expected, builder.toString());
  }

  //private void validateQueryParam(String paramKey, IParameter expected, Collection<?> paramValue) throws InvalidQueryParameterException
  //{
  //    StringBuilder builder = new StringBuilder();
  //    for (Object item : paramValue)
  //    {
  //        builder.append("- ").append(String.valueOf(item)).append("\n");
  //    }
  //    validateQueryParam(paramKey, expected, builder.toString());
  //}

  private void validateQueryParam(String paramKey, IParameter expected, String paramValue) throws TypedException
  {
    if (!expected.validate(paramValue)) {
      String msg = String.format("Invalid value '%s' for query parameter %s. %s",
                                 paramValue, paramKey, expected.message(paramValue));
      throw ApikitErrorTypes.throwErrorTypeNew(new InvalidQueryParameterException(msg));
    }
  }


}
