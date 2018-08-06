/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.recursive;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class RecursiveDefinitionTestCase extends AbstractMultiParserFunctionalTestCase {

  private static final String INVALID_1 =
      "{\n" +
          "  \"value1\": \"root\",\n" +
          "  \"left\": {\n" +
          "    \"value\": \"child1\"\n" +
          "  },\n" +
          "  \"right\": {\n" +
          "    \"value\": \"child2\"\n" +
          "  }\n" +
          "}";

  private static final String INVALID_2 =
      "{\n" +
          "  \"value\": \"root\",\n" +
          "  \"left\": {\n" +
          "    \"value\": \"child1\"\n" +
          "  },\n" +
          "  \"right\": {\n" +
          "    \"value2\": \"child2\"\n" +
          "  }\n" +
          "}";


  private static final String VALID_1 =
      "{\n" +
          "  \"value\": \"root\",\n" +
          "  \"left\": {\n" +
          "    \"value\": \"child1\"\n" +
          "  },\n" +
          "  \"right\": {\n" +
          "    \"value\": \"child2\"\n" +
          "  }\n" +
          "}";

  private static final String VALID_2 =
      "{\n" +
          "  \"value\": \"root\",\n" +
          "  \"left\": {\n" +
          "    \"value\": \"child1\"\n" +
          "  },\n" +
          "  \"right\": {\n" +
          "    \"value\": \"child2\",\n" +
          "    \"left\": {\n" +
          "      \"value\": \"child21\"\n" +
          "    },\n" +
          "    \"right\": {\n" +
          "      \"value\": \"child22\"\n" +
          "    }\n" +
          "  }\n" +
          "}";

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/recursive/mule-config.xml";
  }

  @Test
  public void validPost1() {
    given().header("Content-Type", "application/json")
        .body(VALID_1)
        .expect()
        .response()
        .body(is(VALID_1))
        .statusCode(200)
        .when().post("/api/dummy");
  }

  @Test
  public void validPost2() {
    given().header("Content-Type", "application/json")
        .body(VALID_2)
        .expect()
        .response()
        .body(is(VALID_2))
        .statusCode(200)
        .when().post("/api/dummy");
  }

  @Test
  public void invalidPost1() {
    given().header("Content-Type", "application/json")
        .body(INVALID_1)
        .expect()
        .response()
        .body(is("{message: 'Bad Request'}"))
        .statusCode(400)
        .when().post("/api/dummy");


  }

  @Test
  public void invalidPost2() {

    given().header("Content-Type", "application/json")
        .body(INVALID_2)
        .expect()
        .response()
        .body(is("{message: 'Bad Request'}"))
        .statusCode(400)
        .when().post("/api/dummy");
  }
}
