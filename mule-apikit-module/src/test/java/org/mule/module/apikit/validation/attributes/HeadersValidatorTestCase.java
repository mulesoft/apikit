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
import org.mule.module.apikit.api.exception.InvalidHeaderException;
import org.mule.module.apikit.exception.NotAcceptableException;
import org.mule.raml.implv1.model.ActionImpl;
import org.mule.runtime.api.util.MultiMap;
import org.raml.model.Action;
import org.raml.model.ParamType;
import org.raml.model.parameter.Header;
import org.mule.runtime.api.exception.TypedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadersValidatorTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test(expected = InvalidHeaderException.class)
  public void invalidHeader() throws TypedException, InvalidHeaderException, NotAcceptableException {
    Map<String, Header> expectedHeaders = new HashMap<>();
    Header header1 = new Header();
    header1.setType(ParamType.STRING);
    List<String> values1 = new ArrayList<>();
    values1.add("foo");
    values1.add("var");
    header1.setEnumeration(values1);
    expectedHeaders.put("one", header1);

    Header header2 = new Header();
    header2.setType(ParamType.STRING);
    List<String> values2 = new ArrayList<>();
    values2.add("wow");
    values2.add("yeah");
    header2.setEnumeration(values2);
    expectedHeaders.put("mule-{?}", header2);


    Action action = new Action();
    action.setHeaders(expectedHeaders);
    ActionImpl actionv1 = new ActionImpl(action);
    HeadersValidator validator = new HeadersValidator();

    MultiMap<String, String> incomingHeaders = new MultiMap<>();
    incomingHeaders.put("one", "foo");
    incomingHeaders.put("mule-special", "dough");

    validator.validateAndAddDefaults(incomingHeaders, actionv1, true);
  }

  @Test
  public void validHeader() throws InvalidHeaderException, NotAcceptableException {
    Map<String, Header> expectedHeaders = new HashMap<>();
    Header header1 = new Header();
    header1.setType(ParamType.STRING);
    List<String> values1 = new ArrayList<>();
    values1.add("foo");
    values1.add("var");
    header1.setEnumeration(values1);
    expectedHeaders.put("one", header1);

    Header header2 = new Header();
    header2.setType(ParamType.STRING);
    List<String> values2 = new ArrayList<>();
    values2.add("wow");
    values2.add("yeah");
    header2.setEnumeration(values2);
    expectedHeaders.put("mule-{?}", header2);


    Action action = new Action();
    action.setHeaders(expectedHeaders);
    ActionImpl actionv1 = new ActionImpl(action);
    HeadersValidator validator = new HeadersValidator();

    MultiMap<String, String> incomingHeaders = new MultiMap<>();
    incomingHeaders.put("one", "foo");
    incomingHeaders.put("mule-special", "wow");

    validator.validateAndAddDefaults(incomingHeaders, actionv1, true);
  }

  @Test
  public void sendArrayToANonArrayHeader() throws InvalidHeaderException, NotAcceptableException {
    expectedException.expect(InvalidHeaderException.class);
    expectedException.expectMessage(NON_ARRAY_HEADER_FAIL_MESSAGE);

    Map<String, Header> expectedHeader = new HashMap<>();
    Header header1 = new Header();
    header1.setType(ParamType.STRING);
    expectedHeader.put("one", header1);

    Action action = new Action();
    action.setHeaders(expectedHeader);
    ActionImpl actionv1 = new ActionImpl(action);
    HeadersValidator validator = new HeadersValidator();

    MultiMap<String, String> incomingHeaders = new MultiMap<>();
    incomingHeaders.put("one", Arrays.asList("foo", "wow"));

    validator.validateAndAddDefaults(incomingHeaders, actionv1, true);
  }

  private static final String NON_ARRAY_HEADER_FAIL_MESSAGE = "Header one is not repeatable";
}
