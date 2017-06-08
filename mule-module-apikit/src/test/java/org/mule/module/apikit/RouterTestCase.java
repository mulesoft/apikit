/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.exception.TypedException;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;
import org.junit.Rule;
import org.junit.Test;


import com.jayway.restassured.RestAssured;

@ArtifactClassLoaderRunnerConfig
public class RouterTestCase extends MuleArtifactFunctionalTestCase
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
        return "org/mule/module/apikit/simple-routing/simple.xml";
    }


    @Test
    public void simpleRouting() throws Exception
    {
        given().header("Accept", "*/*")
                .expect()
                .response().body(is("hello"))
                .statusCode(200)
                .when().get("/api/resources");
    }

    @Test
    public void routingWithTypes() throws Exception
    {
        given().header("Content-Type", "application/json")
                .body("{\"name\": \"Fede\"}")
                .expect()
                .response().body(is("hello"))
                .statusCode(200)
                .when().post("/api/types-test");
    }

    @Test
    public void routingReusingPayload() throws Exception
    {
        given().header("Content-Type", "application/json")
                .body("{\"name\": \"Fede\"}")
                .expect()
                .response().body(is("{\"name\": \"Fede\"}"))
                .statusCode(200)
                .when().post("/api/reusing-payload");
    }

    @Test
    public void validSingleAcceptHeader() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body(is("hello"))
                .statusCode(200)
                .when().get("/api/resources");
    }

    @Test
    public void validMultipleAcceptHeader() throws Exception
    {
        given().header("Accept", "application/json, text/plain")
                .expect()
                .response().body(is("hello"))
                .statusCode(200)
                .when().get("/api/resources");
    }

    @Test
    public void validMultipleAcceptHeader2() throws Exception
    {
        given().header("Accept", "text/plain, application/json")
                .expect()
                .response().body(is("hello"))
                .statusCode(200)
                .when().get("/api/resources");
    }

    @Test
    public void invalidSingleAcceptHeader() throws Exception
    {
        given().header("Accept", "application/pepe")
                .expect()
                .response().body(is("{message: 'Not acceptable'}"))
                .statusCode(406)
                .when().get("/api/resources");
    }

    @Test
    public void invalidMultipleAcceptHeader() throws Exception
    {
        given().header("Accept", "application/pepe, text/plain")
                .expect()
                .response().body(is("{message: 'Not acceptable'}"))
                .statusCode(406)
                .when().get("/api/resources");
    }

    @Test
    public void unsupportedMediaType() throws Exception
    {
        given().header("Content-Type", "application/xml")
                .body("<name>Fede</name>")
                .expect()
                .response().body(is("{message: 'Unsupported media type'}"))
                .statusCode(415)
                .when().post("/api/types-test");
    }
}
