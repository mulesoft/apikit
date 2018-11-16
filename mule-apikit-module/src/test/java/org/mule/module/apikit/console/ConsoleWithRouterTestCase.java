/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import io.restassured.RestAssured;
import io.restassured.specification.ResponseSpecification;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

public class ConsoleWithRouterTestCase extends AbstractMultiParserFunctionalTestCase {

  private static final String CONSOLE_BASE_PATH = "/console/";

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/console-with-router.xml";
  }

  @Test
  public void getConsoleIndex() throws Exception {
    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Expires", "-1");

    ResponseSpecification rs = given().port(serverPort.getNumber())
        .header("Accept", "text/html")
        .expect()
        .statusCode(200)
        .headers(headers)
        .contentType("text/html");

    if (isAmfParser()) {
      rs = rs.body(containsString("<title>API console bundle inspector</title>"));
    } else {
      rs = rs.body(startsWith("<!doctype html>"))
          .body(containsString("this.location.href + 'org/mule/module/apikit/console/?raml'"));
    }

    rs.when().get(CONSOLE_BASE_PATH);
  }

  @Test
  public void getConsoleJavascriptResource() throws Exception {
    if (!isAmfParser()) {
      given().port(serverPort.getNumber())
          .header("Accept", "*/*")
          .expect()
          .statusCode(200)
          .header("Access-Control-Allow-Origin", "*")
          .contentType("application/x-javascript")
          .when().get(CONSOLE_BASE_PATH + "bower_components/webcomponentsjs/webcomponents-lite.min.js");
    }
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
    // dump() of wrapper
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
