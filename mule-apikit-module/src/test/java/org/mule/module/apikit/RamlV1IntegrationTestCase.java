/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

public class RamlV1IntegrationTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/raml-handler/simple08.xml";
  }

  @Test
  public void getRamlV1() throws Exception {
    given().header("Accept", "application/raml+yaml")
        .expect()
        .response().body(containsString("RAML 0.8"))
        .statusCode(200)
        .when().get("/console/");
  }

  @Test
  public void notGetRamlV1WithoutUsingHeader() throws Exception {
    given().expect()
        .response().body(not(containsString("RAML 0.8")))
        .statusCode(200)
        .when().get("/console/");
  }
}
