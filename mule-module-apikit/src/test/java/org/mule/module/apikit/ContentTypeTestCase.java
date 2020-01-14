/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ContentTypeTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/contenttype/content-type-config.xml";
    }


    @Test
    public void getOnAcceptAnythingAndNullPayload() throws Exception
    {
        given().header("Accept", "*/*")
            .expect()
                .response().body(is(""))
                .statusCode(200)
            .when().get("/api/resources");
    }

    @Test
    public void getOnUsingInvalidAcceptHeader() throws Exception
    {
        given().header("Accept", "invalid/invalid")
                .expect()
                .response()
                .statusCode(406)
                .when().get("/api/resources");
    }

    @Test
    public void getOnUsingSingleInvalidAcceptHeader() throws Exception
    {
        given().header("Accept", "invalid")
                .expect()
                .response()
                .statusCode(406)
                .when().get("/api/resources");
    }

    @Test
    public void getOnAcceptNotSpecified() throws Exception
    {
        given().header("Accept", "")
            .expect()
                .response().body(is(""))
                .statusCode(200)
            .when().get("/api/resources");
    }

    @Test
    public void getOnAcceptAnythingResponseJson() throws Exception
    {
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
    public void getOnAcceptAnythingResponseXml() throws Exception
    {
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
    public void getOnAcceptAnythingResponseHtml() throws Exception
    {
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
    public void getOnUsingMultipleHttpStatus() throws Exception
    {
        given()
                .header("Accept", "")
                .header("ctype", "zip")
                .expect()
                .response().contentType(is("application/zip"))
                .body(is(""))
                .statusCode(200)
                .when().get("/api/multistatus");

        given()
                .header("Accept", "")
                .expect()
                .response().contentType(is("application/json"))
                .body(is("{ \"message\": \"Data request accepted.\" }"))
                .statusCode(202)
                .when().get("/api/multistatus");
    }

    @Test
    public void honourEncoding() throws Exception
    {
        given()
                .header("Accept", "application/json")
                .expect()
                .response().contentType(is("application/json;charset=UTF-8"))
                .body(is("never mind"))
                .statusCode(200)
                .when().get("/api/encoding");
    }

    @Test
    public void honourEncodingWhenAcceptingAlternativeMimeType() throws Exception
    {
        given()
                .header("Accept", "application/vnd.api+json")
                .expect()
                .response().contentType(is("application/vnd.api+json;charset=UTF-8"))
                .body(is("never mind"))
                .statusCode(200)
                .when().get("/api/encoding");
    }

    @Test
    public void getOnUsingMultipleAcceptHeaderValues() throws Exception {
        given()
                .header("Accept","application/json")
                .header("Accept","application/xml")
                .expect()
                .response()
                .statusCode(200)
                .when().get("/api/multitype");
    }

    @Test
    public void getOnUsingMultipleContentTypeHeaderValues() throws Exception {
        given()
                .header("Content-type","application/json")
                .header("Content-type","application/xml")
                .expect()
                .response()
                .statusCode(415)
                .body(is("unsupported media type"))
                .when().post("/api/multicontenttype");
    }
}
