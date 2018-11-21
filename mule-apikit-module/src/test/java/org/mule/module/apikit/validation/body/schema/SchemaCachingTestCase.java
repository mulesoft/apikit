/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;

public class SchemaCachingTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/body/schema/schema-cache-config.xml";
  }

  @Test
  public void putOnBoth() throws Exception {
    given()
        .body("{\"name\":\"gbs\"}").contentType("application/json")
        .expect().statusCode(204)//204
        .when().put("/cam/currentuser");

    given()
        .body("{\"id\":\"gbs\"}").contentType("application/json")
        .expect().statusCode(204)//204
        .when().put("/peaks/currentuser");
  }

}
