/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.streaming;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class NonRepeatableStreamsTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/streaming/mule-config.xml";
  }

  @Test
  public void simpleUrlencodedRequest() {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "primo")
        .expect()
        .response()
        .body(is("first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-simple");
  }

  @Test
  public void simpleMultipartRequest() {
    given().multiPart("first", "primero")
        .multiPart("second", "segundo")
        .multiPart("third", "true")
        .expect()
        .response()
        .body(is("{\n" +
            "  \"first\": \"primero\",\n" +
            "  \"second\": \"segundo\",\n" +
            "  \"third\": \"true\"\n" +
            "}"))
        .statusCode(201)
        .when().post("/api/multipart");
  }

  @Test
  public void simpleMultipartRequesWithDefaults() {
    given().multiPart("first", "primero")
        .expect()
        .response()
        .body(is("{\n" +
            "  \"first\": \"primero\",\n" +
            "  \"second\": \"segundo\",\n" +
            "  \"third\": \"true\"\n" +
            "}"))
        .statusCode(201)
        .when().post("/api/multipart");
  }

  @Test
  public void simplePostRequest() {
    given().header("Content-Type", "application/json")
        .body("{" +
            "\"firstName\": \"Joe\"," +
            "\"lastName\": \"Doe\"," +
            "\"age\": 20" +
            "}")
        .expect()
        .response()
        .body(is("Joe Doe"))
        .statusCode(201)
        .when().post("/api/simple-post");
  }

}
