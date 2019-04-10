/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static io.restassured.RestAssured.given;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

public class Raml08JsonSchemaRefTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/body/schema/raml-08-with-schema-in-include.xml";
  }

  @Test
  public void JsonSchemaRef() {
    given()
        .body("{\n" + "\"response\":\n" + "{ \"age\": 15 }\n" + "}")
        .contentType("application/json")
        .expect()
        .statusCode(400) // .body(is("bad request"))
        .when()
        .put("/api/jsonschema");
  }
}
