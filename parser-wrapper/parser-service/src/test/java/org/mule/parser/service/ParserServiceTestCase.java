/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.*;
import org.mule.raml.interfaces.model.api.ApiRef;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ParserServiceTestCase {

  @Test
  public void raml08Wrapper() throws URISyntaxException {

    final String api = resource("/api-08.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
  }

  @Test
  public void raml10Wrapper() throws URISyntaxException {

    final String api = resource("/api-10.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void raml10AmfWrapper() throws URISyntaxException {

    final String api = resource("/example-with-include/example-with-include.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.json");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.yaml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasJson20Examples() {

    final String api = resource("/api-with-examples.json");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));

    final IRaml raml = wrapper.build();
    final Map<String, IResource> resources = raml.getResources();

    assertThat(resources.size(), is(2));

    final Map<IActionType, IAction> actions = resources.get("/").getActions();
    assertThat(actions.size(), is(1));

    final IAction action = actions.get(IActionType.GET);
    final Map<String, IResponse> responses = action.getResponses();
    final String example = getExampleWrapper(responses);

    System.out.println("ParserServiceTestCase.oasJson20Example\n" + example);
    assertThat(example, not(isEmptyOrNullString()));

  }

  private static String resource(final String path) {
    URI result = null;

    try {
      result = ParserServiceTestCase.class.getResource(path).toURI();
    } catch (URISyntaxException e) {
      Assert.fail(e.getMessage());
    }
    return result.toString();
  }

  // Same code used for Scaffolding
  private static final String OAS_DEFAULT_STATUS_CODE = "default";
  private String getExampleWrapper(Map<String, IResponse> responses) {
    // filter responses with status codes between 200 and 300 from all responses
    final LinkedHashMap<String, IResponse> validResponses = responses.entrySet().stream()
            //        .filter(entry -> isOkResponse(entry.getKey()))
            .sorted(getStatusCodeComparator())
            .collect(toMap((Map.Entry<String, IResponse> e) -> OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(e.getKey()) ? "200"
                            : e.getKey(),
                    Map.Entry::getValue, (k, v) -> v,
                    LinkedHashMap::new));

    if (validResponses.isEmpty())
      return null;

    // look for an example for status code 200
    final IResponse responseOk = validResponses.get("200");

    String example = null;

    if (responseOk != null)
      example = getExampleFromResponse(responseOk);

    // if there's no examples for status code 200, look for one for any status code
    if (example == null) {
      for (IResponse response : validResponses.values()) {
        example = getExampleFromResponse(response);
        if (example != null)
          break;
      }
    }

    return example;
  }

  private static String getExampleFromResponse(IResponse response) {
    final Map<String, String> examples = response.getExamples();
    if (examples.isEmpty())
      return null;
    if (examples.containsKey("application/json")) {
      return examples.get("application/json");
    } else {
      return examples.values().iterator().next();
    }
  }

  private static Comparator<Map.Entry<String, IResponse>> getStatusCodeComparator() {
    return (c1, c2) -> {
      final String c1Key = c1.getKey();
      final String c2Key = c2.getKey();

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c1Key) && OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c2Key))
        return 0;

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c1Key))
        return -1;

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c2Key))
        return 1;

      return c1Key.compareTo(c2Key);
    };
  }

  private static boolean isOkResponse(final String code) {
    try {
      final Integer value = Integer.valueOf(code);
      return value >= 200 && value < 300;
    } catch (NumberFormatException ignore) {
      return OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(code);
    }
  }
}
