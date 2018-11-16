/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.types;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

public class ScalarTypesValidationTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/types/mule-config.xml";
  }

  @Test
  public void testAlphanumericForStringParam() {
    shouldReturnOkFor("str", "abc123");
  }

  @Test
  public void testSendStrNumberForStringParam() {
    shouldReturnOkFor("str", "1234");
  }

  @Test
  public void testSendNumberForStringParam() {
    shouldReturnOkFor("str", 1234);
  }

  @Test
  public void testSendBooleanForStringParam() {
    shouldReturnOkFor("str", true);
  }

  @Test
  public void testSendDateForStringParam() {
    shouldReturnOkFor("str", "2016-02-28T16:41:41.090Z");
  }

  @Test
  public void testSendNumberForNumberParam() {
    shouldReturnOkFor("num", 1234.9);
  }

  @Test
  public void testSendStrNumberForNumberParam() {
    shouldReturnOkFor("num", "1234.9");
  }

  @Test
  public void testSendIntForIntegerParam() {
    shouldReturnOkFor("int", 1234);
  }

  @Test
  public void testSendStrNumberForIntegerParam() {
    shouldReturnOkFor("int", "1234");
  }

  @Test
  public void testSendBoolForBoolParam() {
    shouldReturnOkFor("bool", true);
  }

  @Test
  public void testSendStrBoolForBoolrParam() {
    shouldReturnOkFor("bool", "false");
  }

  @Test
  public void testSendDateOnlyForDateOnlyParam() {
    shouldReturnOkFor("dateOnly", "2015-05-23");
  }

  @Test
  public void testSendTimeForTimeParam() {
    shouldReturnOkFor("time", "12:30:00");
  }

  @Test
  public void testSendDatetimeOnlyForDatetimeOnlyParam() {
    shouldReturnOkFor("datetimeOnly", "2015-07-04T21:00:00");
  }

  @Test
  public void testSendDatetimeForDatetimeParam() {
    shouldReturnOkFor("datetime", "2016-02-28T16:41:41.090Z");
  }

  private void shouldReturnOkFor(String param, Object value) {
    final String response = "{\n" +
        "  \"" + param + "\": \"" + String.valueOf(value) + "\"\n" +
        "}";

    shouldReturnFor(param, value, 200, response);
  }

  private void shouldReturnFor(String param, Object value, int statusCode, String response) {
    given()
        .queryParam(param, value)
        .expect().response().statusCode(statusCode)
        .body(is(response))
        .when().get("/api/resources");
  }
}
