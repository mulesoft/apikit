/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.console;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class InvalidConsolePath extends FunctionalTestCase
{
    @Rule
    public DynamicPort listenerConsole = new DynamicPort("http.port");
    @Rule
    public DynamicPort secondListenerConsole = new DynamicPort("second.http.port");
    @Rule
    public DynamicPort inboundEndpointConsole = new DynamicPort("inboundEndpointConsole");
    @Rule
    public DynamicPort secondInboundEndpointConsole = new DynamicPort("secondInboundEndpointConsole");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/console/invalid-console-path.xml";
    }

    @Test
    public void getConsoleWithInvalidPathWithListener() throws Exception
    {
        RestAssured.port = listenerConsole.getNumber();
        given().expect()
                .statusCode(500)
                .when().get("/console/");
    }

    @Test
    public void getConsoleWithInboundEndpoint() throws Exception
    {
        RestAssured.port = inboundEndpointConsole.getNumber();
        given().expect()
                .statusCode(200)
                .when().get("/console/");
    }

    @Test
    public void getSecondConsoleWithInvalidPathWithListener() throws Exception
    {
        RestAssured.port = secondListenerConsole.getNumber();
        given().expect()
                .statusCode(500)
                .when().get("/console");
    }

    @Test
    public void getSecondConsoleWithInboundEndpoint() throws Exception
    {
        RestAssured.port = secondInboundEndpointConsole.getNumber();
        given().expect()
                .statusCode(200)
                .when().get("/console");
    }

    @Test
    public void invalidResources()
    {
        RestAssured.port = listenerConsole.getNumber();
        given().header("Accept", "text/html")
                .expect()
                .response().statusCode(404)
                .when().get("console/log4j2.xml");
    }
}
