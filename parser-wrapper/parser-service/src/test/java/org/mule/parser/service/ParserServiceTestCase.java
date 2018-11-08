/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.ApiRef;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.IResponse;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ParserServiceTestCase {

  @Test
  public void raml08Wrapper() throws URISyntaxException {

    final URI api = resource("/api-08.raml");

    final ParserWrapper wrapper = ParserService.create(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
  }

  @Test
  public void raml10Wrapper() throws URISyntaxException {

    final URI api = resource("/api-10.raml");

    final ParserWrapper wrapper = ParserService.create(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() throws URISyntaxException {

    final URI api = resource("/petstore.json");

    final ParserWrapper wrapper = ParserService.create(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() throws URISyntaxException {

    final URI api = resource("/petstore.yaml");

    final ParserWrapper wrapper = ParserService.create(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Ignore
  public void oasJson20Examples() throws URISyntaxException {

    final URI api = resource("/api-with-examples.json");

    final ParserWrapper wrapper = ParserService.create(ApiRef.create(api), ParserType.AMF);
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

  private static URI resource(final String path) {
    URI result = null;
    try {
      result = ParserServiceTestCase.class.getResource(path).toURI();
    } catch (URISyntaxException e) {
      Assert.fail(e.getMessage());
    }
    return result;
  }

  // Same code used for Scaffolding
  private String getExampleWrapper(Map<String, IResponse> responses) {

    IResponse response = responses.get("200");

    if (response == null || response.getBody() == null) {
      for (IResponse response1 : responses.values()) {
        if (response1.getBody() != null) {
          Map<String, IMimeType> responseBody1 = response1.getBody();
          IMimeType mimeType = responseBody1.get("application/json");
          if (mimeType != null && mimeType.getExample() != null) {
            return mimeType.getExample();
          } else {
            for (IMimeType type : responseBody1.values()) {
              if (type.getExample() != null) {
                return type.getExample();
              }
            }
          }
        }
      }
    }

    if (response != null && response.getBody() != null) {
      Map<String, IMimeType> body = response.getBody();
      IMimeType mimeType = body.get("application/json");
      if (mimeType != null && mimeType.getExample() != null) {
        return mimeType.getExample();
      }

      for (IMimeType mimeType2 : response.getBody().values()) {
        if (mimeType2 != null && mimeType2.getExample() != null) {
          return mimeType2.getExample();
        }
      }
    }

    return null;

  }
}
