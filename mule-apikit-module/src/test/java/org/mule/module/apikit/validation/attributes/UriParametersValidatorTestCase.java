/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import org.junit.Ignore;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class UriParametersValidatorTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/validation/uriParameters/resource/mule-config.xml";
  }

  @Test
  public void answer200WhenInlineRequiredUriParameterIsValid() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/resource/24");
  }

  @Test
  public void answer200WhenSpecialCharactesAreSent() throws Exception {
    given().urlEncodingEnabled(false)
        .pathParam("src", "this%20is%20a%20test")
        .expect()
        .response()
        .statusCode(200)
        .body(is("this is a test"))
        .when().get("api/escaped/{src}");
  }

  @Test
  public void answer200WhenInlineOptionalUriParameterIsValid() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/list/24");
  }

  @Test
  public void answer400WhenInlineRequiredUriParameterIsInvalid() throws Exception {
    given().expect().response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().get("api/resource/hello");
  }

  @Test
  public void answer400WhenInlineOptionalUriParameterIsInvalid() throws Exception {
    given().expect().response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().get("api/list/hello");
  }

  @Test
  public void answer404WhenRequiredUriParameterIsNotPresent() throws Exception {
    given().expect().response()
        .statusCode(404)
        .body(is("{message: 'Not Found'}"))
        .when().get("api/resource");
  }

  @Test
  @Ignore("APIKIT-935: 404 Not Found when resource contains optional URI Parameters")
  public void answer200WhenInlineOptionalUriParameterIsNotPresent() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/list");
  }

  @Test
  public void answer200WhenOptionalUriParameterIsNotPresent() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/uriparam");
  }

  @Test
  public void answer400WhenOptionalUriParameterIsInvalid() throws Exception {
    given().expect().response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().get("api/uriparam/asd");
  }

  @Test
  public void answer200WhenOptionalUriParameterIsValid() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/uriparam/1234");
  }

  @Test
  public void answer200WhenDateUriParameterIsValid() throws Exception {
    given().expect().response()
        .statusCode(200)
        .when().get("api/dateParam/2016-02-28T16:41:41.090Z");
  }

}
