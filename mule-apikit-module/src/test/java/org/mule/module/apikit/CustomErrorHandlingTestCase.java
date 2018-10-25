/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class CustomErrorHandlingTestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/custom-error/simple.xml";
  }


  @Test
  public void testCustomErrorHandling() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("{message: 'Bad request'}"))
        .statusCode(400)
        .when().get("/api/resource");
  }

  @Test
  public void testVariablesPropagationOnErrorHandling() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("{message: 'Not Found'}"))
        .statusCode(404)
        .when().get("/api/error");
  }

}
