/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class Console0000Replacement08TestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  private String CONSOLE_BASE_PATH = "/console/";

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
    return "org/mule/module/apikit/console/console-0000-replacement-raml08.xml";
  }

  @Test
  public void getConsoleIndex() throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Expires", "-1");

    given().port(serverPort.getNumber())
        .header("Accept", "text/html")
        .expect()
        .statusCode(200)
        .headers(headers)
        .contentType("text/html")
        .body(startsWith("<!doctype html>"))
        .body(containsString("this.location.href + '?raml'"))
        .when().get(CONSOLE_BASE_PATH);
  }

  @Test
  public void getRootRaml() {
    given().port(serverPort.getNumber())
        .header("Accept", "application/raml+yaml")
        .expect()
        .header("Content-Type", "application/raml+yaml")
        .response()
        .statusCode(200)
        .body(containsString("/types-test:"))
        .body(containsString("baseUri: http://localhost"))
        .when().get("console/org/mule/module/apikit/console/?raml");
  }

}
