/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class ConsoleWithKRBUWithBaseUriTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/console-with-krbu-with-baseuri.xml";
  }

  @Test
  public void getRootRamlConsoleWithoutRouter() {
    // dump() of wrapper
    given().port(serverPort.getNumber())
        .header("Accept", "application/raml+yaml")
        .expect()
        .header("Content-Type", "application/raml+yaml")
        .response()
        .statusCode(200)
        .body(containsString("/types-test:"))
        .body(containsString("baseUri: http://www.google.com"))
        .when().get("consoleWithoutRouter/org/mule/module/apikit/console/?raml");
  }


  @Test
  public void getRootRamlConsolelWithRouter() {
    // dump() of wrapper
    given().port(serverPort.getNumber())
        .header("Accept", "application/raml+yaml")
        .expect()
        .header("Content-Type", "application/raml+yaml")
        .response()
        .statusCode(200)
        .body(containsString("/types-test:"))
        .body(containsString("baseUri: http://www.google.com"))
        .when().get("consoleWithRouter/org/mule/module/apikit/console/?raml");
  }
}
