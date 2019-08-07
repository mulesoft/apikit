/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

public class ConsoleApiReferencesWithReferences extends FunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");


  @Override
  public int getTestTimeoutSecs()
  {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception
  {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigResources()
  {
    return "org/mule/module/apikit/console/console-api-references-with-references.xml";
  }

  @Test
  public void console()
  {
    given().header("Accept", "text/html")
        .expect()
        .response().body(containsString("<title>API Console</title>"))
        .header("Content-type", "text/html").statusCode(200)
        .when().get("console/index.html");
  }

  @Test
  public void consoleResource()
  {
    given().header("Accept", "text/css")
        .expect()
        .response().body(containsString(".CodeMirror"))
        .header("Content-type", "text/css").statusCode(200)
        .when().get("console/styles/api-console-light-theme.css");
  }


  @Test
  public void apiResources()
  {
    String[] apiResources = new String[]{"refs-directory/properties.raml","refs-directory/ref-with-include.raml"};

    for (String resource: apiResources){
      given().header("Accept", "*/*")
          .expect().response().statusCode(200)
          .when().get("console/api/" + resource);
    }
  }
}
