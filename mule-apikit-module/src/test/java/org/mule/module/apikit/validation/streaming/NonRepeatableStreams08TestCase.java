/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.streaming;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class NonRepeatableStreams08TestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/streaming/mule-config-08.xml";
  }

  @Test
  public void multipartRequest() {
    given().header("Content-Type", "multipart/form-data")
        .multiPart("code", "R2D2")
        .expect()
        .response()
        .body(is("{\n" +
            "  \"code\": \"R2D2\",\n" +
            "  \"color\": \"black\"\n" +
            "}"))
        .when().delete("/api/multipart");
  }

  @Test
  public void urlencodedRequest() {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("Id", 12345)
        .expect()
        .response()
        .body(is("Id=12345&Size=medium"))
        .statusCode(200)
        .when().put("/api/urlencoded");
  }



}
