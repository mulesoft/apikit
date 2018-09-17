/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.empty;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;

@ArtifactClassLoaderRunnerConfig
public class EmptyBodyRaml10TestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/validation/body/empty/mule-config-10.xml";
  }

  @Test
  public void successWhenMethodInPutRequestIsEmpty() throws Exception {
    given().expect()
        .statusCode(200)
        .when().put("/api/method-without-body");
  }

  @Test
  public void successWhenMethodInPostRequestIsEmpty() throws Exception {
    given().expect()
        .statusCode(200)
        .when().post("/api/method-without-body");
  }

  @Test
  public void successWhenBodyWithoutContentTypeInPostRequestIsEmpty() throws Exception {
    given().header("Content-Type", "application/json")
        .expect()
        .statusCode(200)
        .when().post("/api/method-and-body-without-content-type");
  }

  @Test
  public void successWhenBodyWithoutContentTypeInPutRequestIsEmpty() throws Exception {
    given().header("Content-Type", "application/json")
        .expect()
        .statusCode(200)
        .when().put("/api/method-and-body-without-content-type");
  }

  @Test
  public void successWhenBodyWithoutSchemaInPostIsEmpty() throws Exception {
    given().header("Content-Type", "application/json")
        .expect()
        .statusCode(200)
        .when().post("/api/body-with-empty-content-type");
  }

  @Test
  public void successWhenBodyWithoutSchemaInPutIsEmpty() throws Exception {
    given().header("Content-Type", "application/json")
        .expect()
        .statusCode(200)
        .when().put("/api/body-with-empty-content-type");
  }

  @Test
  public void invalidMediaTypeWhenBodyWithoutSchemaInPostIsEmpty() throws Exception {
    given().header("Content-Type", "application/xml")
        .expect()
        .statusCode(415)
        .when().post("/api/body-with-empty-content-type");
  }

  @Test
  public void invalidMediaTypeWhenBodyWithoutSchemaInPutIsEmpty() throws Exception {
    given().header("Content-Type", "application/xml")
        .expect()
        .statusCode(415)
        .when().put("/api/body-with-empty-content-type");
  }
}
