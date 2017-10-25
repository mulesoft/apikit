/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

@ArtifactClassLoaderRunnerConfig(sharedRuntimeLibs = {"org.mule.tests:mule-tests-unit"})
public class ConsoleWithoutRouterTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");
  @Rule
  public DynamicPort serverPort2 = new DynamicPort("serverPort2");

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
    return "org/mule/module/apikit/console/console-without-router.xml";
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
        .body(containsString("this.location.href + 'org/mule/module/apikit/console/?raml'"))
        .when().get(CONSOLE_BASE_PATH);
  }

  @Test
  public void getConsoleJavascriptResource() throws Exception {
    given().port(serverPort.getNumber())
        .header("Accept", "*/*")
        .expect()
        .statusCode(200)
        .header("Access-Control-Allow-Origin", "*")
        .contentType("application/x-javascript")
        .when().get(CONSOLE_BASE_PATH + "bower_components/webcomponentsjs/webcomponents-lite.min.js");
  }


  @Test
  public void consoleFileNotFound() throws Exception {
    RestAssured.port = serverPort.getNumber();
    given().port(serverPort.getNumber())
        .header("Accept", "*/*")
        .expect()
        .statusCode(500)
        .when().get(CONSOLE_BASE_PATH + "not/found/file.html");
  }

  @Test
  public void getIndexWithRedirect() {
    given().port(serverPort.getNumber()).redirects().follow(false)
        .header("Accept", "text/html")
        .expect()
        .header("Location", "http://localhost:" + serverPort.getNumber() + "/console/")
        .response().statusCode(301)
        .when().get("/console");
  }

  @Test
  public void getRamlFile() {
    given().port(serverPort.getNumber())
        .header("Accept", "application/raml+yaml")
        .expect()
        .header("Content-Type", "application/raml+yaml")
        .response()
        .statusCode(200)
        .body(containsString("/types-test:"))
        .when().get("console/org/mule/module/apikit/console/simple-with-baseuri10.raml?raml");
  }

  @Test
  public void getRamlFileWithoutQueryParameter() {
    given().port(serverPort.getNumber())
        .header("Accept", "application/raml+yaml")
        .expect()
        .header("Content-Type", "application/raml+yaml")
        .response()
        .statusCode(200)
        .body(containsString("/types-test:"))
        .when().get("console/org/mule/module/apikit/console/simple-with-baseuri10.raml");
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
        .body(containsString("baseUri: http://www.google.com"))
        //keepRamlBaseUri is false, but there is no router to retrieve the api source. The raml baseUri is returned.
        .when().get("console/org/mule/module/apikit/console/?raml");
  }

}
