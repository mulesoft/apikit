/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters.uri;

import io.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static io.restassured.RestAssured.given;

@ArtifactClassLoaderRunnerConfig
public class ResolveUriVersionParameterTestCase extends MuleArtifactFunctionalTestCase {

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
    return "org/mule/module/apikit/parameters/uri/uri-parameters-config.xml";
  }

  @Test
  public void failWhenInvokingEndpointWithoutVersion() {
    given()
        .expect()
        .statusCode(404)
        .when().get("api/resource/invalid-version");
  }

  @Test
  public void successWhenInvokingEndpointWithVersion() {
    given()
        .expect()
        .statusCode(200)
        .when().get("api/resource/v1");
  }
}
