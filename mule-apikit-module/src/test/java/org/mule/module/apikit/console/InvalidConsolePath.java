/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.console;

import io.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static io.restassured.RestAssured.given;

public class InvalidConsolePath extends AbstractMultiParserFunctionalTestCase {

  @Rule
  public DynamicPort secondServerPort = new DynamicPort("secondServerPort");
  @Rule
  public DynamicPort inboundEndpointConsole = new DynamicPort("inboundEndpointConsole");
  @Rule
  public DynamicPort secondInboundEndpointConsole = new DynamicPort("secondInboundEndpointConsole");

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/console-invalid-path.xml";
  }

  @Test
  public void getConsoleWithInvalidPath() throws Exception {
    RestAssured.port = serverPort.getNumber();
    given().expect()
        .statusCode(500)
        .when().get("/console/");

    given().expect()
        .statusCode(500)
        .when().get("/console");

  }
}
