/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import com.jayway.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;

public class Console0000Replacement08TestCase extends AbstractMultiParserFunctionalTestCase {

  private static final String CONSOLE_BASE_PATH = "/console/";

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/console-0000-replacement-raml08.xml";
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
          .body(containsString("this.location.href + '?raml'"));
    }

    rs.when().get(CONSOLE_BASE_PATH);
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
