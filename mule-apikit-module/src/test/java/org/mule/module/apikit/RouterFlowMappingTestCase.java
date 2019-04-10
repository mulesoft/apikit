/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;

public class RouterFlowMappingTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/simple-routing/simple-flow-mapping.xml";
  }

  @Test
  public void simpleRouting() throws Exception {
    given()
        .header("Accept", "*/*")
        .expect()
        .response()
        .body(is("hello"))
        .statusCode(200)
        .when()
        .get("/api/resources");
  }
}
