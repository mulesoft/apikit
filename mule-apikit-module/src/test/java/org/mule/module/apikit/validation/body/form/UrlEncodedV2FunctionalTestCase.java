/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import org.junit.Ignore;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class UrlEncodedV2FunctionalTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/formParameters/mule-config-v2.xml";
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
  public void simpleInvalidUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "primo")
        .expect()
        .response()
        .body(is("{message: 'Bad Request'}"))
        .statusCode(400)
        .when().post("/api/url-encoded-simple-integer");
  }

  @Test
  @Ignore // TODO Support adding default parameters
  public void setDefaultFormParameterForUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("second", "segundo")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("first=primo"))
        .statusCode(201)
        .when().post("/api/url-encoded-with-default");
  }

  @Test
  public void getKeyWithMultipleValuesUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "hello")
        .formParam("second", "segundo")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("first=hello&second=segundo&third=true"))
        .statusCode(201)
        .when().post("/api/url-encoded-duplicated-key");
  }

  @Test
  public void getKeyWithDuplicatedValuesUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "hello")
        .formParam("second", "segundo")
        .formParam("second", "segundo2")
        .formParam("third", "true")
        .expect()
        .response()
        .body(is("{message: 'Bad Request'}"))
        .statusCode(400)
        .when().post("/api/url-encoded-duplicated-key");
  }

  @Test
  public void getKeyWithArraysUrlencodedRequest() throws Exception {
    given().header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("first", "1234")
        .formParam("first", "5678")
        .formParam("second", "1234")
        .formParam("second", "5678")
        .formParam("third", "1234")
        .expect()
        .response()
        .body(is("first=1234&first=5678&second=1234&second=5678&third=1234"))
        .statusCode(201)
        .when().post("/api/url-encoded-with-arrays");
  }

}
