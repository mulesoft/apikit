/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spacesInPath;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;

public class SpacesInPathTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/space in path/mule-config.xml";
  }

  @Test
  public void successWhenRamlResourcePathContainsSpaces() {
    given()
        .body("{\"response\": {\"name\": \"eleo\",\"age\": 15}}")
        .contentType("application/json")
        .expect().statusCode(200)
        .when().put("/api/schema");
  }

}
