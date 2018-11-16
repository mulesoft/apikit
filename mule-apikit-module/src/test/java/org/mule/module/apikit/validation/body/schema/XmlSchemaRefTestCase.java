/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;

public class XmlSchemaRefTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/body/schema/xml-schema-ref-config.xml";
  }

  @Test
  public void validSchema() throws Exception {
    given()
        .body("<message xmlns=\"http://www.example.org/simple\" item=\"hola\"/>").contentType("application/xml")
        .expect().statusCode(200)
        .when().put("/api/name");
  }

  @Test
  public void invalidSchema() throws Exception {
    given()
        .body("{\"name\":\"gbs\"}").contentType("application/xml")
        .expect().statusCode(400)
        .when().put("/api/name");
  }

  @Test
  public void validGlobalSchema() throws Exception {
    given()
        .body("<message xmlns=\"http://www.example.org/simple\" item=\"hola\"/>").contentType("application/xml")
        .expect().statusCode(200)
        .when().put("/api/last");
  }

}
