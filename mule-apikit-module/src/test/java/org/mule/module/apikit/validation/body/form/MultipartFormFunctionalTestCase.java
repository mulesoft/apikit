/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertTrue;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class MultipartFormFunctionalTestCase extends MuleArtifactFunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/validation/formParameters/mule-config-v1.xml";
    }
    @Test
    public void answer400WhenRequiredFormParameterIsNotProvided() throws Exception
    {
        given().multiPart("Unnecessary", "Form Parameter")
                .expect()
                .response()
                .statusCode(400)
                .body(is("{message: 'Bad Request'}"))
                .when().post("/api/users");
    }

    @Test
    @Ignore("Form parameter types are not being validated [APIKIT-839]")
    public void answer400WhenRequiredFormParameterIsInvalid() throws Exception
    {
        given().multiPart("userId", "I am not an integer HAHAHA")
                .expect()
                .response()
                .statusCode(400)
                .body(is("{message: 'Bad Request'}"))
                .when().post("/api/users");
    }

    @Test
    public void answer200WhenRequiredFormParameterIsValid() throws Exception
    {
        given().multiPart("userId", 5101)
                .expect()
                .response()
                .statusCode(200)
                .body(is("5101"))
                .when().post("/api/users");
    }

    @Test
    public void answer200WhenMultipleRequiredFormParameterAreProvided() throws Exception
    {
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
    public void answer200WhenMultipleOptionalFormParameterAreNotProvidedAndAdded() throws Exception
    {
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
    public void answer200WhenOptionalFormParameterIsNotProvided() throws Exception
    {
        given().multiPart("Unnecessary", "Form Parameter")
                .expect()
                .response()
                .statusCode(200)
                .body(is("content-not-provided"))
                .when().post("/api/announcements");
    }

    @Test
    @Ignore("Form parameter types are not being validated [APIKIT-839]")
    public void answer400WhenOptionalFormParameterIsInvalid() throws Exception
    {
        given().multiPart("content", "More than 10 characters")
                .expect()
                .response()
                .statusCode(400)
                .body(is("{message: 'Bad Request'}"))
                .when().post("/api/announcements");
    }

    @Test
    public void answer200WhenOptionalFormParameterIsValid() throws Exception
    {
        given().multiPart("content", "Is Valid")
                .expect()
                .response()
                .statusCode(200)
                .body(is("Is Valid"))
                .when().post("/api/announcements");
    }

    @Test
    public void setDefaultFormParameterForMultipartRequest() throws Exception
    {
        given().multiPart("first", "primero", "application/json")
                .multiPart("third", "true")
                .multiPart("payload", "3.4")
                .expect().response()
                .body(is("segundo")).statusCode(201)
                .when().post("/api/multipart");
    }

    @Test
    public void postTextFileResourceIntoMultiPartFormData() throws Exception
    {
        given().multiPart("document", "lorem.txt", this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/validation/formParameters/lorem.txt"))
                .expect()
                .response()
                .statusCode(200)
                .contentType("text/plain")
                .body(startsWith("Lorem ipsum dolor sit amet"))
                .when().post("/api/uploadFile");
    }


    @Test
    @Ignore // TODO investigate how to return encoded images using data weave
    public void postImageResourceIntoMultiPartFormData() throws Exception
    {
        byte[] imageInByteArray = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/validation/formParameters/bbva.jpg"));
        String result = Arrays.toString(imageInByteArray);

        given().multiPart("image", "bbva.jpg", this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/validation/formParameters/bbva.jpg"))
                .expect()
                .response()
                .statusCode(200)
                .body(is(result))
                .when().post("/api/uploadImage");
    }
}
