/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class ProcessorsAfterRouterTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/simple-routing/processors-after-router.xml";
  }

  @Test
  public void simpleRoutingAndSettingPayloadAfterwards() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .header("firstHeader", "value1")
        .header("secondHeader", "value2")
        .response().body(is("goodbye"))
        .statusCode(200)
        .when().get("/api/resources");
  }

  @Test
  public void invalidSingleAcceptHeader() throws Exception {
    given().header("Accept", "application/pepe")
        .expect()
        .response().body(is("{message: 'Not acceptable'}"))
        .statusCode(406)
        .when().get("/api/resources");
  }
}
