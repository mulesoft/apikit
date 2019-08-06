/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;

public class ContentTypeWithDifferentCasesTestCase extends FunctionalTestCase {

  private static final String VALID_BODY = "{\"firstname\": \"Juan\", \"lastname\": \"Desi\"}";
  private static final String INVALID_BODY = "{\"firstname\": \"Juan\"}";

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigResources() {
    return "org/mule/module/apikit/contenttype/uppercase-content-type-routing-config.xml";
  }

  @Test
  public void endpointWithUpperCaseContentTypeAcceptsUpperCaseContentTypeInRequest() throws Exception {
    given().body(VALID_BODY)
      .contentType("Application/JSON")
      .expect().statusCode(200)
      .when().post("/api/upper");
  }

  @Test
  public void endpointWithUpperCaseContentTypeAcceptsLowerCaseContentTypeInRequest() throws Exception {
    given().body(VALID_BODY)
      .contentType("application/json")
      .expect().statusCode(200)
      .when().post("/api/upper");
  }

  @Test
  public void endpointWithLowerCaseContentTypeAcceptsUpperCaseContentTypeInRequest() throws Exception {
    given().body(VALID_BODY)
      .contentType("Application/JSON")
      .expect().statusCode(200)
      .when().post("/api/lower");
  }

  @Test
  public void endpointWithLowerCaseContentTypeAcceptsLowerCaseContentTypeInRequest() throws Exception {
    given().body(VALID_BODY)
      .contentType("application/json")
      .expect().statusCode(200)
      .when().post("/api/lower");
  }

  @Test
  public void endpointWithLowerCaseContentTypeAcceptsLowerCaseContentTypeInRequestFailsToValidate() throws Exception {
    given().body(INVALID_BODY)
      .contentType("application/json")
      .expect().statusCode(400)
      .when().post("/api/lower");
  }

  @Test
  public void endpointWithUpperCaseContentTypeAcceptsLowerCaseContentTypeInRequestFailsToValidate() throws Exception {
    given().body(INVALID_BODY)
      .contentType("application/json")
      .expect().statusCode(400)
      .when().post("/api/upper");
  }

  @Test
  public void endpointWithUpperCaseContentTypeAcceptsUpperCaseContentTypeInRequestFailsToValidate() throws Exception {
    given().body(INVALID_BODY)
      .contentType("application/JSON")
      .expect().statusCode(400)
      .when().post("/api/upper");
  }

  @Test
  public void endpointRequestAndFlowContainsContentTypeWithDifferentCasesFailsToValidate() throws Exception {
    given().body(INVALID_BODY)
      .contentType("application/JSON")
      .expect().statusCode(400)
      .when().post("/api/flowDifferentContentTypeCase");
  }

  @Test
  public void endpointRequestAndFlowContainsContentTypeWithDifferentCases() throws Exception {
    given().body(VALID_BODY)
      .contentType("application/JSON")
      .expect().statusCode(200)
      .when().post("/api/flowDifferentContentTypeCase");
  }
}
