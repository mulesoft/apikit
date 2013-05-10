/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.base;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
@Ignore
public class BaseResourceFunctionalTestCase extends FunctionalTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/resource/base/base-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void updateNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .header("Content-Length", "0")
            .when()
            .put("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .header("Content-Length", "0")
            .when()
            .put("/api/");
    }

    @Test
    public void updateNotSupportedSwaggerDisabled() throws Exception
    {
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .put("/apiSwaggerDisabled");
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .put("/apiSwaggerDisabled/");
    }

    @Test
    public void createNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Content-Length", "0")
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .post("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Content-Length", "0")
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .post("/api/");
    }

    @Test
    public void createNotSupportedSwaggerDisabled() throws Exception
    {
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .post("/apiSwaggerDisabled");
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .post("/apiSwaggerDisabled/");
    }

    @Test
    public void deleteNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .header("Content-Length", "0")

            .when()
            .delete("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .header("Content-Length", "0")
            .when()
            .delete("/api/");
    }

    @Test
    public void deleteNotSupportedSwaggerDisabled() throws Exception
    {
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .delete("/apiSwaggerDisabled");
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .delete("/apiSwaggerDisabled/");
    }

    @Test
    public void retrieve() throws Exception
    {
        given().header("Accept", "text/html").expect().response().statusCode(200).when().get("/api");
        given().header("Accept", "text/html").expect().response().statusCode(200).when().get("/api/");
    }

    @Test
    public void retrieveSwaggerDisabled() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .get("/apiSwaggerDisabled");
        given().header("Accept", "text/html")
            .expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .get("/apiSwaggerDisabled/");
    }

    @Test
    public void retrieveUnauthorized() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
            .response()
            .header("Content-Length", "0")
            .statusCode(401)
            .when()
            .get("/protectedapi");
        given().header("Accept", "text/html")
            .expect()
            .response()
            .header("Content-Length", "0")
            .statusCode(401)
            .when()
            .get("/protectedapi/");
    }

    @Test
    public void exists() throws Exception
    {
        given().expect().response().statusCode(200).header("Content-Length", "0").when().head("/api");
        given().expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/");
    }

    @Test
    public void existsSwagerDisabled() throws Exception
    {
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .head("/apiSwaggerDisabled");
        given().expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .head("/apiSwaggerDisabled/");
    }

    @Test
    public void retreiveUnsupportedContentTypes() throws Exception
    {
        given().header("Accept", "application/json")
            .expect()
            .response()
            .statusCode(406)
            .header("Content-Length", "0")
            .when()
            .get("/api");
        given().header("Accept", "text/pain")
            .expect()
            .response()
            .statusCode(406)
            .header("Content-Length", "0")
            .when()
            .get("/api");
    }

    @Test
    public void retreiveUnsupportedContentTypesSwagerDisabled() throws Exception
    {
        given().header("Accept", "application/json")
            .expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .get("/apiSwaggerDisabled");
        given().header("Accept", "text/pain")
            .expect()
            .response()
            .statusCode(404)
            .header("Content-Length", "0")
            .when()
            .get("/apiSwaggerDisabled");
    }

}
