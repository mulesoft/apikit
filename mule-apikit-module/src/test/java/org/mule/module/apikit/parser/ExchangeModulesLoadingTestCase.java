/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class ExchangeModulesLoadingTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/exchange/mule-config.xml";
  }

  @Test
  public void getResource1() throws Exception {
    given().header("Accept", "application/json")
        .expect()
        .response().body(containsString("{\n" +
            "  \"name\" : \"a Name\",\n" +
            "  \"length\" : 2999\n" +
            "}"))
        .header("Content-Type", "application/json").statusCode(200)
        .when().get("/api/resource1");
  }

  @Test
  public void getResource2() throws Exception {
    given().header("Accept", "application/json")
        .expect()
        .response().body(containsString("{\n" +
            "  \"id\": \"an id\",\n" +
            "  \"field1\": {\n" +
            "    \"id\" : \"an id\",\n" +
            "    \"name\" : \"a Name\"\n" +
            "  }\n" +
            "}"))
        .header("Content-type", "application/json").statusCode(200)
        .when().get("/api/resource2");
  }

  @Test
  public void getResource3() throws Exception {
    given().header("Accept", "application/json")
        .expect()
        .response().body(containsString("{\n" +
            "  \"id\" : \"an id\",\n" +
            "  \"name\" : \"a Name\"\n" +
            "}"))
        .header("Content-type", "application/json").statusCode(200)
        .when().get("/api/resource3");
  }

}
