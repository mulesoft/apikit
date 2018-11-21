/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import io.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class RouterOasWithValidationTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/router-oas-with-validation/api.xml";
  }

  @Test
  public void getPet() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is(""))
        .statusCode(200)
        .when().get("/api/amf/oas/pet");
  }

  @Test
  public void getPetById() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().get("/api/amf/oas/pet/123");
  }

  @Test
  public void getPetByIdWithInvalidId() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is(""))
        .statusCode(500)
        .when().get("/api/amf/oas/pet/abc");
  }

  @Test
  public void getPetFindByStatus() throws Exception {
    given().header("Accept", "*/*")
        .queryParam("status", "pending")
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().get("/api/amf/oas/pet/findByStatus");
  }

  @Test
  public void getPetFindByStatusWithInvalidStatus() throws Exception {
    given().header("Accept", "*/*")
        .queryParam("status", "invalid-status")
        .expect()
        .response()
        .body(is(""))
        .statusCode(500)
        .when().get("/api/amf/oas/pet/findByStatus");
  }

  @Test
  public void getPetFindByStatusWithMissingStatus() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response()
        .body(is(""))
        .statusCode(500)
        .when().get("/api/amf/oas/pet/findByStatus");
  }

  /*
    No encuentra el metodo put. No esta cargado en el FlowFinder.
    Falla en FlowFinder line 169 -> if (action.hasBody() && action.getBody().get(type) != null) 
  */
  @Ignore
  public void putPetByIdUsingJson() throws Exception {
    given().header("Content-Type", "application/json")
        .header("api_key", "ASDFGHJKL")
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().put("/api/amf/oas/pet/123");
  }

  /*
    No encuentra el metodo put. No esta cargado en el FlowFinder.
    Falla en FlowFinder line 169 -> if (action.hasBody() && action.getBody().get(type) != null) 
  */
  @Ignore
  public void putPetByIdUsingXml() throws Exception {
    given().header("Content-Type", "application/xml")
        .header("api_key", API_KEY)
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().put("/api/amf/oas/pet/123");
  }

  @Test
  public void postPetUsingJson() throws Exception {
    given().header("Content-Type", "application/json")
        .body(PET_POST_JSON)
        .expect()
        .response()
        .body(is(PET_POST_JSON))
        .statusCode(201)
        .when().post("/api/amf/oas/pet");
  }

  @Ignore
  public void postPetUsingXml() throws Exception {
    given().header("Content-Type", "application/xml")
        .body(PET_POST_XML)
        .expect()
        .response()
        .body(is(PET_POST_XML))
        .statusCode(201)
        .when().post("/api/amf/oas/pet");
  }

  @Test
  public void deletePetById() throws Exception {
    given().header("Accept", "*/*")
        .header("api_key", API_KEY)
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().delete("/api/amf/oas/pet/123");
  }


  @Test
  public void getUser() throws Exception {
    given().header("Accept", "*/*")
        .header("token", TOKEN)
        .expect()
        .response().body(is(""))
        .statusCode(200)
        .when().get("/api/amf/oas/user");
  }

  @Test
  public void getUserByNameId() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().get("/api/amf/oas/user/abc");
  }

  @Test
  public void putUserByNameUsingJson() throws Exception {
    given().header("Content-Type", "application/json")
        .body(USER_POST_JSON)
        .expect()
        .response()
        .body(is(USER_POST_JSON))
        .statusCode(200)
        .when().put("/api/amf/oas/user/abc");
  }

  @Ignore
  public void putUserByNameUsingXml() throws Exception {
    given().header("Content-Type", "application/xml")
        .body(USER_POST_XML)
        .expect()
        .response()
        .body(is(USER_POST_XML))
        .statusCode(200)
        .when().put("/api/amf/oas/user/abc");
  }


  @Test
  public void postUserUsingJson() throws Exception {
    given().header("Content-Type", "application/json")
        .body(USER_POST_JSON)
        .expect()
        .response()
        .body(is(USER_POST_JSON))
        .statusCode(200)
        .when().post("/api/amf/oas/user");
  }

  @Ignore
  public void postUserUsingXml() throws Exception {
    given().header("Content-Type", "application/xml")
        .body(USER_POST_XML)
        .expect()
        .response()
        .body(is(USER_POST_XML))
        .statusCode(201)
        .when().post("/api/amf/oas/user");
  }

  @Test
  public void deleteUserByName() throws Exception {
    given().header("Accept", "*/*")
        .header("api_key", API_KEY)
        .expect()
        .response()
        .body(is(""))
        .statusCode(200)
        .when().delete("/api/amf/oas/user/abc");
  }

  private static final String API_KEY = "ASDFGHJKL";
  private static final String TOKEN = "1234567890";

  private static String PET_POST_JSON = "{\n" +
      "  \"name\": \"A name\",\n" +
      "  \"photoUrls\": [\"http://a.ml\"]\n" +
      "}";

  private static String PET_POST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<Pet>\n" +
      "   <name>A name</name>\n" +
      "   <photoUrls>\n" +
      "      <element>http://a.ml</element>\n" +
      "   </photoUrls>\n" +
      "</Pet>";

  private static String USER_POST_JSON = "{\n" +
      "  \"id\": 0,\n" +
      "  \"username\": \"ale\",\n" +
      "  \"firstName\": \"alejandro\",\n" +
      "  \"lastName\": \"amura\",\n" +
      "  \"email\": \"ale.amura\",\n" +
      "  \"password\": \"12345\",\n" +
      "  \"phone\": \"12345678\",\n" +
      "  \"userStatus\": 1\n" +
      "}";

  private static String USER_POST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<User>\n" +
      "  <id>0</id>\n" +
      "  <username>ale</username>\n" +
      "  <firstName>alejandro</firstName>\n" +
      "  <lastName>amura</lastName>\n" +
      "  <email>ale.amura</email>\n" +
      "  <password>12345</password>\n" +
      "  <phone>12345678</phone>\n" +
      "  <userStatus>1</userStatus>\n" +
      "</User>";
}
