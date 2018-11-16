/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class CustomErrorHandlingTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/custom-error/simple.xml";
  }

  @Test
  public void testCustomErrorHandling() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("{message: 'Bad request'}"))
        .statusCode(400)
        .when().get("/api/resource");
  }

  @Test
  public void testVariablesPropagationOnErrorHandling() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("{message: 'Not Found'}"))
        .statusCode(404)
        .when().get("/api/error");
  }

}
