/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.api.exception.InvalidQueryParameterException;
import org.mule.raml.implv1.model.ActionImpl;
import org.mule.runtime.api.util.MultiMap;
import org.raml.model.Action;
import org.raml.model.ParamType;
import org.raml.model.parameter.QueryParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QueryParametersValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test(expected = InvalidQueryParameterException.class)
  public void invalidQueryParamMaxLength() throws InvalidQueryParameterException {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("first", "first");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=first", true);
  }

  @Test
  public void validQueryParamMaxLength() throws InvalidQueryParameterException {
    Map<String, QueryParameter> expectedQueryParams = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParams.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParams);
    ActionImpl actionImpl = new ActionImpl(action);

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a", true);
  }

  @Test
  public void validQueryParamAddingDefault() throws InvalidQueryParameterException {
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

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a", true);
    assertEquals("a", validator.getQueryParams().get("first"));
    assertEquals("test", validator.getQueryParams().get("second"));
    assertEquals("first=a&second=test", validator.getQueryString());
  }

  @Test
  public void validQueryParamAddingDefaultWithSpaces() throws InvalidQueryParameterException {
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

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("first", "a");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=a", true);
    assertEquals("a", validator.getQueryParams().get("first"));
    assertEquals("test with spaces", validator.getQueryParams().get("second"));
    assertEquals("first=a&second=test+with+spaces", validator.getQueryString());
  }

  @Test
  public void failWhenSendingArrayToANonArrayQueryParam() throws InvalidQueryParameterException {
    expectedException.expect(InvalidQueryParameterException.class);
    expectedException.expectMessage(NON_ARRAY_QUERY_PARAM_FAIL_MESSAGE);

    Map<String, QueryParameter> expectedQueryParam = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParam.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParam);
    ActionImpl actionImpl = new ActionImpl(action);

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("first", Arrays.asList("foo", "wow"));

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "first=foo&first=wow", true);
  }

  @Test
  public void failWhenSendingNonDefinedQueryParam() throws InvalidQueryParameterException {
    expectedException.expect(InvalidQueryParameterException.class);
    expectedException.expectMessage(STRICT_VALIDATION_FAIL_MESSAGE);

    Map<String, QueryParameter> expectedQueryParam = new HashMap<>();
    QueryParameter queryParam1 = new QueryParameter();
    queryParam1.setType(ParamType.STRING);
    queryParam1.setMaxLength(1);
    expectedQueryParam.put("first", queryParam1);

    Action action = new Action();
    action.setQueryParameters(expectedQueryParam);
    ActionImpl actionImpl = new ActionImpl(action);

    MultiMap<String, String> incomingQueryParams = new MultiMap<>();
    incomingQueryParams.put("second", "b");

    QueryParameterValidator validator = new QueryParameterValidator(actionImpl);
    validator.validateAndAddDefaults(incomingQueryParams, "second=b", true);
  }

  private static final String NON_ARRAY_QUERY_PARAM_FAIL_MESSAGE = "Query parameter first is not repeatable";
  private static final String STRICT_VALIDATION_FAIL_MESSAGE = "second parameters are not defined in RAML.";
}
