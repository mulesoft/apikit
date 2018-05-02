/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.streaming;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class NonRepeatableStreamsTestCase extends MuleArtifactFunctionalTestCase {

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
