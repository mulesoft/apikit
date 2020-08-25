/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;

import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

public class ParametersNullableTestCase extends FunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  private static final String validateNullStringType = "apikit.validate.null.string.param";

  @Override
  public int getTestTimeoutSecs()
  {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception
  {
    setProperty(validateNullStringType, "true");
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigFile()
  {
    return "org/mule/module/apikit/parameters/parameters-10-config.xml";
  }

  @After
  public void clear()
  {
    clearProperty(validateNullStringType);
  }

  @Test
  public void stringTypeNullInvalid()
  {
    given().queryParam("string-type", (String) null)
        .expect().response().statusCode(500)
        .when().get("/api/repeat");
  }

  @Test
  public void nullableIngerType()
  {
    given().queryParam("integer-nil-type", (String) null)
        .expect().response().statusCode(200)
        .when().get("/api/repeat");
  }

  @Test
  public void nullableIngerTypeWithNullAsString()
  {
    given().queryParam("integer-nil-type", "null")
        .expect().response().statusCode(200)
        .when().get("/api/repeat");
  }

  @Test
  public void nonNullableIngerType()
  {
    given().queryParam("integer-type", (String) null)
        .expect().response().statusCode(500)
        .when().get("/api/repeat");
  }

  @Test
  public void nonNullableIngerTypeWithNullAsString()
  {
    given().queryParam("integer-type", "null")
        .expect().response().statusCode(500)
        .when().get("/api/repeat");
  }
}
