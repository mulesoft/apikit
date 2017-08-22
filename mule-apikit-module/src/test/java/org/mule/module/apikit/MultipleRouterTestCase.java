/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class MultipleRouterTestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/multiple-router/multiple-raml.xml";
  }


  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*").body("{\"age\": \"1\"}").contentType("application/json")
        .expect()
        .response().body(is("typesDog"))
        .statusCode(200)
        .when().post("/api1/typesDog");

    given().header("Accept", "*/*").body("{\"name\": \"a\"}").contentType("application/json")
        .expect()
        .response().body(is("typesPerson"))
        .statusCode(200)
        .when().post("/api2/typesPerson");

    given().header("Accept", "*/*").body("hello").contentType("application/xml")
        .expect()
        .response().body(is("{message: 'Unsupported media type'}"))
        .statusCode(415)
        .when().post("/api1/typesDog");

    given().header("Accept", "*/*").body("hello").contentType("application/xml")
        .expect()
        .response().body(is("{message: 'Unsupported media type'}"))
        .statusCode(415)
        .when().post("/api2/typesPerson");
  }


}
