/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mule.module.apikit.Configuration.APPLICATION_RAML;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class StandaloneConsoleKeepingBaseUriTestCase extends FunctionalTestCase
{

    private static final String CONSOLE_PATH = "/konsole";

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    @Rule
    public DynamicPort serverPort2 = new DynamicPort("serverPort2");
    @Rule
    public DynamicPort serverPort3 = new DynamicPort("serverPort3");
    @Rule
    public DynamicPort serverPort4 = new DynamicPort("serverPort4");
    @Rule
    public DynamicPort serverPort5 = new DynamicPort("serverPort5");

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
        return "org/mule/module/apikit/console/standalone-console-config-keep-base-uri.xml";
    }

    @Test
    public void console() throws Exception
    {
        given().header("Accept", "text/html")
                .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       containsString("src=\"./?\"")))
                .header("Content-type", "text/html").statusCode(200)
                .when().get(CONSOLE_PATH + "/index.html");
    }

    @Test
    public void getRaml() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: \"http://localhost/originalapi\""))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/api");
    }

    @Test
    public void getRamlUsingConsole() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: \"http://localhost/originalapi\""))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/konsole");
    }

    @Test
    public void consoleNoPath() throws Exception
    {
        given().port(serverPort2.getNumber()).header("Accept", "text/html")
                .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       containsString("src=\"./?\"")))
                .header("Content-type", "text/html").statusCode(200)
                .when().get("/index.html");
    }


    @Test
    public void getRamlConsoleNoPath() throws Exception
    {
        given().port(serverPort2.getNumber()).header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: \"http://localhost/originalapi\""))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/");
    }

    @Test
    public void getRamlConsoleBindAllInterfaces() throws Exception
    {
        given().port(serverPort4.getNumber()).header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: \"http://localhost/originalapi\""))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/");
    }

}
