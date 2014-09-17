/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class DefaultParametersTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/parameters/default-parameters-config.xml";
    }

    @Test
    public void defaultHeader() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().header("one", "bar").statusCode(200)
                .when().get("/api/headers");
    }

    @Test
    public void defaultQueryParameter() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().header("default", "1").statusCode(200)
                .when().get("/api/queryParams");
    }

    @Test
    public void defaultFormParameterMultipart() throws Exception
    {
        given().multiPart("first", "primero")
                .multiPart("third", "true")
                .multiPart("payload", "3.4")
                .expect().response().header("second", "segundo").statusCode(201)
                .when().post("/api/multipart");
    }

    @Test
    public void defaultFormParameterUrlencoded() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("second", "segundo")
                .formParam("third", "true")
                .expect().response().header("first", "primo").statusCode(201)
                .when().post("/api/url-encoded");
    }
}
