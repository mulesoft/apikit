/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import java.util.List;
import org.mule.raml.interfaces.model.api.ApiRef;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

abstract class AbstractTestCase {

  private static final String MISSING_RESOURCE = "Resource '%s' missing in AMF Resources for uri '%s'";
  private static final String MISSING_ACTION = "Action '%s' missing";
  private static final String MISSING_PARAMETER = "Parameter '%s' missing";

  static void assertEqual(final IRaml actual, final IRaml expected) {

    assertThat(actual.getVersion(), is(equalTo(expected.getVersion())));
    assertThat(actual.getBaseUri(), is(equalTo(expected.getBaseUri())));

    assertParametersEqual(actual.getBaseUriParameters(), expected.getBaseUriParameters());

    //dump("Resources 08",  ramlResources);
    //dump("Resources AMF",  amfResources);
    assertResourcesEqual(actual.getResources(), expected.getResources());

    // assertEqual(actual.getAllReferences(), expected.getAllReferences());

    // TODO"
    //schemas()

    //"Different behaviour in Java Parser 08 & 10"
    // cleanBaseUriParameters()
    // consolidatedSchemas()
    // instance()
    // getSecuritySchemes()
    // getTraits()
    // getUri()
  }

  static void assertEqual(final List<ApiRef> actual, final List<ApiRef> expected) {
    assertThat(actual.size(), is(expected.size()));
  }

  static void assertResourcesEqual(final Map<String, IResource> actual, final Map<String, IResource> expected) {

    final String actualKeys = mkString(actual.keySet());
    final String expectedKeys = mkString(expected.keySet());
    assertThat("expected: '" + expectedKeys + "' but was '" + actualKeys + "", actual.size(), is(expected.size()));

    actual.forEach((k, resource) -> {
      assertThat(format(MISSING_RESOURCE, k, resource.getUri()), expected.containsKey(k), is(true));
      assertEqual(resource, expected.get(k));
    });
  }

  static String mkString(final Set<String> set) {
    return set.stream().collect(joining(", "));
  }

  static void assertEqual(final IResource actual, final IResource expected) {
    assertThat(actual.getUri(), is(equalTo(expected.getUri())));
    assertThat(actual.getRelativeUri(), is(equalTo(expected.getRelativeUri())));
    assertThat(actual.getParentUri(), is(equalTo(expected.getParentUri())));
    assertThat(actual.getResolvedUri("v10"), is(equalTo(expected.getResolvedUri("v10"))));
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
    assertActionsEqual(actual.getActions(), expected.getActions());
    actual.getActions().keySet().forEach(action -> {
      final String actualAction = actual.getAction(action.name()).getType().name();
      final String expectedAction = expected.getAction(action.name()).getType().name();
      assertThat(actualAction, is(equalTo(expectedAction)));

    });
    assertParametersEqual(actual.getResolvedUriParameters(), expected.getResolvedUriParameters());
    assertResourcesEqual(actual.getResources(), expected.getResources());

    // Different behaviour in Java Parser 08 & 10
    // Map<String, List<IParameter>> getBaseUriParameters();
    // void setParentUri(String parentUri); 
    // void cleanBaseUriParameters();
  }

  static void assertActionsEqual(final Map<IActionType, IAction> actual, final Map<IActionType, IAction> expected) {

    assertThat(actual.size(), is(expected.size()));

    actual.forEach((k, v) -> {
      assertThat(format(MISSING_ACTION, k), expected.containsKey(k), is(true));
      assertEqual(v, expected.get(k));
    });
  }

  static void assertEqual(final IAction actual, final IAction expected) {
    assertThat(actual.getType(), is(equalTo(expected.getType())));

    assertParametersEqual(actual.getHeaders(), expected.getHeaders());
    // TODO MORE cases
    //actual.getBody();
    //actual.getResource();        
  }

  static void assertParametersEqual(final Map<String, IParameter> actual, final Map<String, IParameter> expected) {

    final String actualKeys = mkString(actual.keySet());
    final String expectedKeys = mkString(expected.keySet());
    assertThat("expected: '" + expectedKeys + "' but was '" + actualKeys + "", actual.size(), is(expected.size()));

    actual.forEach((k, v) -> {
      assertThat(format(MISSING_PARAMETER, k), expected.containsKey(k), is(true));
      assertEqual(v, expected.get(k));
    });
  }

  static void assertEqual(final IParameter actual, final IParameter expected) {
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));

    assertThat(actual.getDefaultValue(), is(equalTo(expected.getDefaultValue())));
    assertThat(actual.isRepeat(), is(expected.isRepeat()));
    assertThat(actual.isArray(), is(expected.isArray()));
    //  boolean validate(String value);
    //  String message(String value);
    // Different behaviour in Java Parser 08 & 10
    //assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
    assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));
    assertThat(actual.getExample(), is(equalTo(expected.getExample())));
    assertExamplesEqual(actual.getExamples(), expected.getExamples());

    // Different behaviour in Java Parser 08 & 10
    // Object getInstance();
    // MetadataType getMetadata();

  }

  static void assertExamplesEqual(final Map<String, String> actual, final Map<String, String> expected) {
    assertThat(actual.size(), is(expected.size()));
  }

  private static void dump(final String title, Map<String, IResource> resources) {
    System.out.println(format("------------- %s -------------", title));
    System.out.println(dump("", resources, ""));
    System.out.println("-------------------------------------");
  }

  private static String dump(final String indent, Map<String, IResource> resources, String out) {

    if (resources.isEmpty())
      return out;

    for (Map.Entry<String, IResource> entry : resources.entrySet()) {

      final IResource value = entry.getValue();
      final Set<String> actions = value.getActions().keySet().stream().map(Enum::name).collect(toSet());
      final String resource = "[" + entry.getKey() + "] -> " + value.getUri() + " " + mkString(actions);
      out += indent + resource + "\n";
      if (value.getResources().isEmpty())
        continue;

      out = dump(indent + "  ", value.getResources(), out);
    }
    return out;
  }
}
