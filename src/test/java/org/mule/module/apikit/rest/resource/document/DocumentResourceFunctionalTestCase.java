/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.document;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class DocumentResourceFunctionalTestCase extends FunctionalTestCase
{

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
        return "org/mule/module/apikit/rest/resource/document/document-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void documentNotFound() throws Exception
    {
        expect().response().statusCode(404).header("Content-Length", "0").when().head("/api/league1");
        expect().response().statusCode(404).header("Content-Length", "0").when().head("/api/league1/");
    }

    @Test
    public void createNotAllowed() throws Exception
    {
        expect().response().statusCode(405).header("Content-Length", "0").when().post("/api/league");
    }

    @Test
    public void retrieve() throws Exception
    {
        expect().response().statusCode(200).body(containsString("Liga BBVA")).when().get("/api/league");
    }

    @Test
    public void update() throws Exception
    {
        given().body("Premier League").expect().response().statusCode(200).when().put("/api/league");
    }

    @Test
    public void deleteNotAllowed() throws Exception
    {
        expect().response().statusCode(405).header("Content-Length", "0").when().delete("/api/league");
    }

    @Test
    public void exists() throws Exception
    {
        expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/league");
    }

    @Test
    public void swagger() throws Exception
    {
        given().header("Accept", "application/json")
            .expect()
            .log()
            .everything()
            .response()
            .statusCode(200)
            .when()
            .get("/api/league");
    }

    // Nested

    @Test
    public void nestedDocumentNotFound() throws Exception
    {
        expect().response().statusCode(404).header("Content-Length", "0").when().get("/api/league/bla");
        expect().response().statusCode(404).header("Content-Length", "0").when().get("/api/league/bla/");
    }

    @Test
    public void createNotAllowedOnNestedDocument() throws Exception
    {
        expect().response()
            .statusCode(405)
            .header("Content-Length", "0")
            .when()
            .post("/api/league/association");
        expect().response()
            .statusCode(405)
            .header("Content-Length", "0")
            .when()
            .post("/api/league/association/");
    }

    @Test
    public void retrieveOnNestedDocument() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(containsString("Royal"))
            .when()
            .get("/api/league/association");
        expect().response()
            .statusCode(200)
            .body(containsString("Royal"))
            .when()
            .get("/api/league/association/");
    }

    @Test
    public void existsOnNestedDocument() throws Exception
    {
        expect().response()
            .statusCode(200)
            .header("Content-Length", "0")
            .when()
            .head("/api/league/association");
        expect().response()
            .statusCode(200)
            .header("Content-Length", "0")
            .when()
            .head("/api/league/association/");
    }

    @Test
    public void updateOnNestedDocument() throws Exception
    {
        given().body("AFA").expect().response().statusCode(200).when().put("/api/league/association");
        given().body("AFA").expect().response().statusCode(200).when().put("/api/league/association/");
    }

    @Test
    public void deleteNotAllowedOnNestedDocument() throws Exception
    {
        expect().response()
            .statusCode(405)
            .header("Content-Length", "0")
            .when()
            .delete("/api/league/association");
        expect().response()
            .statusCode(405)
            .header("Content-Length", "0")
            .when()
            .delete("/api/league/association/");
    }

}
