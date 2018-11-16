/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class UrlEncodedV1FunctionalTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/formParameters/mule-config-v1.xml";
  }

  @Test
  public void simpleUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "primo")
        .expect()
        .response()
        .body(is("first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-simple");
  }

  @Test
  public void setDefaultFormParameterForUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("second", "segundo")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("second=segundo&third=true&first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-with-default");
  }

  @Test
  public void getKeyWithMultipleValuesUrlencodedRequest() throws Exception {
    String body = "second=segundo&second=segundo2&third=true&first=primo";
    int status = 201;

    if (isAmfParser()) {
      status = 400;
      body = "{message: 'Bad Request'}";
    }

    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("second", "segundo")
        .formParam("second", "segundo2")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is(body))
        .statusCode(status)
        .when().post("/api/url-encoded-duplicated-key");
  }

}
