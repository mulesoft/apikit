/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import static org.junit.Assert.assertEquals;

import org.mule.module.apikit.exception.InvalidQueryParameterException;
import org.mule.module.apikit.validation.attributes.QueryParameterValidator;
import org.mule.raml.implv1.model.ActionImpl;
import org.mule.runtime.core.exception.TypedException;
import org.mule.runtime.http.api.domain.ParameterMap;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.raml.model.Action;
import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

public class QueryParametersValidatorTestCase {

  @Test(expected = TypedException.class)
  public void invalidQueryParamMaxLength() throws TypedException
  {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    ParameterMap incomingQueryParams = new ParameterMap();
    incomingQueryParams.put("first", "first");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=first");
  }

  @Test
  public void validQueryParamMaxLength() throws TypedException
  {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    ParameterMap incomingQueryParams = new ParameterMap();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a");
  }

  @Test
  public void validQueryParamAddingDefault() throws TypedException
  {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    QueryParameter queryParam2 = new QueryParameter();
    queryParam2.setType(ParamType.STRING);
    queryParam2.setDefaultValue("test");
    expectedQueryParams.put("second", queryParam2);


    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    ParameterMap incomingQueryParams = new ParameterMap();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a");
    assertEquals("a", validator.getQueryParams().get("first"));
    assertEquals("test", validator.getQueryParams().get("second"));
    assertEquals("first=a&second=test", validator.getQueryString());
  }

  @Test
  public void validQueryParamAddingDefaultWithSpaces() throws TypedException
  {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    QueryParameter queryParam2 = new QueryParameter();
    queryParam2.setType(ParamType.STRING);
    queryParam2.setDefaultValue("test with spaces");
    expectedQueryParams.put("second", queryParam2);


    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    ParameterMap incomingQueryParams = new ParameterMap();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a");
    assertEquals("a", validator.getQueryParams().get("first"));
    assertEquals("test with spaces", validator.getQueryParams().get("second"));
    assertEquals("first=a&second=test+with+spaces", validator.getQueryString());
  }
}
