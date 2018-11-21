/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class UriParametersInMethodsTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/uriParameters/action/mule-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Test
  public void testValidStringValueForStringParameter() {
    given().expect().response()
        .statusCode(200)
        .when().get("api/test/someId");
  }

  @Test
  public void testValidIntegerValueForStringParameter() {
    given().expect().response()
        .statusCode(200)
        .when().get("api/test/24");
  }

  @Test
  public void testValidIntegerValueForIntegerParameter() {
    given().expect().response()
        .statusCode(200)
        .when().delete("api/test/24");
  }

  @Test
  public void testInvalidValueForIntegerParameter() {
    given().expect().response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().delete("api/test/someId");
  }

}
