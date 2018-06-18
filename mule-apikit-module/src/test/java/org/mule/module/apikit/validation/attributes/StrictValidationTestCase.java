/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;

public class StrictValidationTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/strict-validation/strict-validation-config.xml";
  }

  @Test
  public void failWhenSendingNonDefinedQueryParam() throws Exception {
    given().queryParam("param2", "value")
        .expect()
        .statusCode(400)
        .when().get("api/resource");
  }

  @Test
  public void successWhenSendingDefinedQueryParam() throws Exception {
    given().queryParam("param1", "value")
        .expect()
        .statusCode(200)
        .when().get("api/resource");
  }

  @Test
  public void failWhenSendingNonDefinedHeader() throws Exception {
    given().header("header2", "value")
        .expect()
        .statusCode(400)
        .when().get("api/resource");
  }

  @Test
  public void successWhenSendingDefinedHeader() throws Exception {
    given().header("header1", "value")
        .expect()
        .statusCode(200)
        .when().get("api/resource");
  }
}
