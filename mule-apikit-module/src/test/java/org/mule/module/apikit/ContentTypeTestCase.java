/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public abstract class ContentTypeTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/contenttype/content-type-config.xml";
  }

  @Test
  public void getOnAcceptAnythingAndNullPayload() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is(""))
        .statusCode(200)
        .when().get("/api/resources");
  }

  @Test
  public void getOnUsingInvalidAcceptHeader() throws Exception {
    given().header("accept", "invalid/invalid")
        .expect()
        .response()
        .statusCode(406)
        .when().get("/api/resources");
  }

  @Test
  public void getOnUsingSingleInvalidAcceptHeader() throws Exception {
    given().header("Accept", "invalid")
        .expect()
        .response()
        .statusCode(406)
        .when().get("/api/resources");
  }

  @Test
  public void getOnAcceptNotSpecified() throws Exception {
    given().header("Accept", "*")
        .expect()
        .response().body(is(""))
        .statusCode(200)
        .when().get("/api/resources");
  }

  @Test
  public void getOnAcceptAnythingResponseJson() throws Exception {
    given()
        .header("Accept", "")
        .header("ctype", "json")
        .expect()
        .response().contentType(is("application/json"))
        .body(is("never mind"))
        .statusCode(200)
        .when().get("/api/multitype");
  }

  @Test
  public void getOnAcceptAnythingResponseXml() throws Exception {
    given()
        .header("Accept", "")
        .header("ctype", "xml")
        .expect()
        .response().contentType(is("application/xml"))
        .body(is("never mind"))
        .statusCode(200)
        .when().get("/api/multitype");
  }

  @Test
  public void getOnAcceptAnythingResponseHtml() throws Exception {
    given()
        .header("Accept", "")
        .header("ctype", "default")
        .expect()
        .response().contentType(is("text/html"))
        .body(is("never mind"))
        .statusCode(200)
        .when().get("/api/multitype");
  }

  @Test
  public void getOnUsingMultipleHttpStatus() throws Exception {
    given()
        .header("Accept", "*/*")
        .header("ctype", "zip")
        .expect()
        .response().contentType(is("application/zip"))
        .body(is(""))
        .statusCode(200)
        .when().get("/api/multistatus");

    given()
        .header("Accept", "*/*")
        .expect()
        .response().contentType(is("application/json"))
        .body(is("{ \"message\": \"Data request accepted.\" }"))
        .statusCode(202)
        .when().get("/api/multistatus");
  }
}
