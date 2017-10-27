/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mule.module.apikit.api.RamlHandler.APPLICATION_RAML;

import com.jayway.restassured.response.Response;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;
import org.junit.Rule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

@ArtifactClassLoaderRunnerConfig
public class RouterRamlProcessorsAfterRouterTestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/router-raml/processors-after-router.xml";
  }

  @Test
  public void simpleRouting() throws Exception {
    given().expect()
        .response().body(is("goodbye")) //payload is crushed by the processor located after Router
        .header("firstHeader", "value1")
        .header("secondHeader", "value2")
        .statusCode(200)
        .when().get("/api/resources");
  }
}
