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

public class NoStrictValidationTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/strict-validation/no-strict-validation-config.xml";
  }

  @Test
  public void successWhenSendingNonDefinedQueryParam() throws Exception {
    given().queryParam("noDefinedParam", "value")
        .expect()
        .statusCode(200)
        .when().get("api/resource");
  }

  @Test
  public void successWhenSendingNonDefinedHeader() throws Exception {
    given().header("noDefinedHeader", "value")
        .expect()
        .statusCode(200)
        .when().get("api/resource");
  }

}
