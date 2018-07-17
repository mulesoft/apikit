/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.module.apikit.AbstractMultiParserFunctionalTestCase;
import org.mule.runtime.core.api.util.IOUtils;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

public abstract class MultipartFormFunctionalTestCase extends AbstractMultiParserFunctionalTestCase {

  @Test
  public void answer400WhenRequiredFormParameterIsNotProvided() throws Exception {
    given().multiPart("Unnecessary", "Form Parameter")
        .expect()
        .response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().post("/api/users");
  }

  @Test
  public void answer400WhenRequiredFormParameterIsInvalid() throws Exception {
    given().multiPart("userId", "I am not an integer HAHAHA")
        .expect()
        .response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().post("/api/users");
  }

  @Test
  public void answer200WhenRequiredFormParameterIsValid() throws Exception {
    given().multiPart("userId", 5101)
        .expect()
        .response()
        .statusCode(200)
        .body(is("5101"))
        .when().post("/api/users");
  }

  @Test
  public void answer200WhenMultipleRequiredFormParameterAreProvided() throws Exception {
    given().multiPart("userId", 5101)
        .multiPart("second", "segundo")
        .multiPart("third", true)
        .expect()
        .response()
        .statusCode(201)
        .body(is("[\n" +
            "  \"userId\",\n" +
            "  \"second\",\n" +
            "  \"third\"\n" +
            "]"))
        .when().post("/api/multiple-required-multipart");
  }

  @Test
  public void answer200WhenMultipleOptionalFormParameterAreNotProvidedAndAdded() throws Exception {
    given().multiPart("userId", 5101)
        .expect()
        .response()
        .statusCode(201)
        .body(is("[\n" +
            "  \"userId\",\n" +
            "  \"second\",\n" +
            "  \"third\"\n" +
            "]"))
        .when().post("/api/multiple-optional-multipart");
  }

  @Test
  public void answer200WhenOptionalFormParameterIsNotProvided() throws Exception {
    given().multiPart("Unnecessary", "Form Parameter")
        .expect()
        .response()
        .statusCode(200)
        .body(is("content-not-provided"))
        .when().post("/api/announcements");
  }

  @Test
  public void answer400WhenOptionalFormParameterIsInvalid() throws Exception {
    given().multiPart("content", "More than 10 characters")
        .expect()
        .response()
        .statusCode(400)
        .body(is("{message: 'Bad Request'}"))
        .when().post("/api/announcements");
  }

  @Test
  public void answer200WhenOptionalFormParameterIsValid() throws Exception {
    given().multiPart("content", "Is Valid")
        .expect()
        .response()
        .statusCode(200)
        .body(is("Is Valid"))
        .when().post("/api/announcements");
  }

  @Test
  public void setDefaultFormParameterForMultipartRequest() throws Exception {
    given().multiPart("first", "primero", "application/json")
        .multiPart("payload", "3.4")
        .expect().response()
        .body(is("{\n" +
            "  \"first\": \"primero\",\n" +
            "  \"payload\": \"3.4\",\n" +
            "  \"second\": \"segundo\",\n" +
            "  \"third\": \"true\"\n" +
            "}"))
        .statusCode(201)
        .when().post("/api/multipart");
  }

  @Test
  public void postTextFileResourceIntoMultiPartFormData() throws Exception {
    given().multiPart("document", "lorem.txt", this.getClass().getClassLoader()
        .getResourceAsStream("org/mule/module/apikit/validation/formParameters/lorem.txt"))
        .expect()
        .response()
        .statusCode(200)
        .contentType("text/plain")
        .body(startsWith("Lorem ipsum dolor sit amet"))
        .when().post("/api/uploadFile");
  }


  @Test
  @Ignore // TODO investigate how to return encoded images using data weave
  public void postImageResourceIntoMultiPartFormData() throws Exception {
    byte[] imageInByteArray = IOUtils.toByteArray(this.getClass().getClassLoader()
        .getResourceAsStream("org/mule/module/apikit/validation/formParameters/bbva.jpg"));
    String result = Arrays.toString(imageInByteArray);

    given().multiPart("image", "bbva.jpg", this.getClass().getClassLoader()
        .getResourceAsStream("org/mule/module/apikit/validation/formParameters/bbva.jpg"))
        .expect()
        .response()
        .statusCode(200)
        .body(is(result))
        .when().post("/api/uploadImage");
  }

  @Test
  public void answer201WhenOptionalFormParameterIsProvidedAsEmpty() throws Exception {
    given().multiPart("first", "required")
        .multiPart("third", "false")
        .multiPart("fourth", "")
        .expect()
        .response()
        .statusCode(201)
        .body(is("{\n" +
            "  \"first\": \"required\",\n" +
            "  \"third\": \"false\",\n" +
            "  \"fourth\": \"\",\n" +
            "  \"second\": \"segundo\"\n" +
            "}"))
        .when().post("/api/multipart");
  }
}
