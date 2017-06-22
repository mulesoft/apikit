/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;


import org.mule.raml.implv1.model.ActionImpl;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.exception.TypedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.raml.model.Action;
import org.raml.model.ParamType;
import org.raml.model.parameter.Header;

public class HeadersValidatorTestCase {

  @Test(expected = TypedException.class)
  public void invalidHeader() throws TypedException
  {
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

    validator.validateAndAddDefaults(incomingHeaders, actionv1);
  }

  @Test
  public void validHeader() throws TypedException
  {
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

    validator.validateAndAddDefaults(incomingHeaders, actionv1);
  }
}
