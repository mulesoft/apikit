/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class RamlV2IntegrationTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/raml-handler/simple10.xml";
  }

  @Test
  public void getRootRamlV2() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(containsString("RAML 1.0"))
        .statusCode(200)
        .when().get("/console/org/mule/module/apikit/raml-handler/?raml");
  }

  @Test
  public void getExampleV2() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(containsString("jane"))
        .statusCode(200)
        .when().get("/console/org/mule/module/apikit/raml-handler/example.json/?raml");
  }
}
