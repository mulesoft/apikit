/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;

public class QueryParametersTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/parameters/query-parameters/query-parameters-config.xml";
  }

  @Test
  public void stringArrayQueryParamWithAsterisk() {
    given().queryParam("string", "*a", "b")
        .expect().response().statusCode(200)
        .when().get("/api/resource");
  }

  @Test
  public void stringArrayQueryParamNumber() {
    given().queryParam("string", "12", "12")
        .expect().response().statusCode(200)
        .when().get("/api/resource");
  }
}
