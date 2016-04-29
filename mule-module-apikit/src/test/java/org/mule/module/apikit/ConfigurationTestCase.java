/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ConfigurationTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPortDefault = new DynamicPort("serverPortDefault");
    @Rule
    public DynamicPort serverPortCustom = new DynamicPort("serverPortCustom");
    @Rule
    public DynamicPort serverPortNoConsole = new DynamicPort("serverPortNoConsole");
    @Rule
    public DynamicPort serverPortMapping = new DynamicPort("serverPortMapping");
    @Rule
    public DynamicPort serverPortNoValidations = new DynamicPort("serverPortNoValidations");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/config/configuration-test-config.xml";
    }

    @Test
    public void resourceOnDefaultConfig() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given().header("Accept", "text/plain")
                .expect()
                .response().body(is("some resources"))
                .statusCode(200)
                .when().get("/default/resource");
    }

    @Test
    public void consoleOnDefaultConfig() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given().header("Accept", "text/html")
                .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       containsString("src=\"./?\"")))
                .header("Content-type", "text/html").statusCode(200)
                .when().get("/default/console/");
    }

    @Test
    public void consoleResourceOnDefaultConfig() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given().header("Accept", "text/css")
                .expect()
                .response().body(containsString(".CodeMirror"))
                .header("Content-type", "text/css").statusCode(200)
                .when().get("/default/console/styles/api-console-light-theme.css");
    }

    @Test
    public void resourceOnCustomConsolePathConfig() throws Exception
    {
        RestAssured.port = serverPortCustom.getNumber();
        given()
                .expect()
                .response().body(is("some resources"))
                .statusCode(200)
                .when().get("/custom/resource");
    }

    @Test
    public void resourceOnExplicitMappingConfig() throws Exception
    {
        RestAssured.port = serverPortMapping.getNumber();
        given()
                .expect()
                .response().body(is("explicitely mapped flow"))
                .statusCode(200)
                .when().get("/mapping/resource");
    }

    @Test
    public void consoleOnCustomConsolePathConfig() throws Exception
    {
        RestAssured.port = serverPortCustom.getNumber();
        given().header("Accept", "text/html")
                .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       containsString("src=\"./?\"")))
                .header("Content-type", "text/html").statusCode(200)
                .when().get("/custom/custom/");
    }

    @Test
    public void consoleResourceOnCustomConsolePathConfig() throws Exception
    {
        RestAssured.port = serverPortCustom.getNumber();
        given().header("Accept", "text/css")
                .expect()
                .response().body(containsString(".CodeMirror"))
                .header("Content-type", "text/css").statusCode(200)
                .when().get("/custom/custom/styles/api-console-light-theme.css");
    }

    @Test
    public void resourceOnNoConsoleConfig() throws Exception
    {
        RestAssured.port = serverPortNoConsole.getNumber();
        given()
                .expect()
                .response().body(is("some resources"))
                .statusCode(200)
                .when().get("/no-console/resource");
    }

    @Test
    public void consoleOnNoConsoleConfig() throws Exception
    {
        RestAssured.port = serverPortNoConsole.getNumber();
        given().header("Accept", "text/html")
                .expect()
                .response().body(containsString("resource not found"))
                .statusCode(404)
                .when().get("/no-console/console/");
    }

    @Test
    public void patch() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given()
                .expect().statusCode(204)
                .when().patch("/default/resource");
    }

    @Test
    public void enableValidations()
    {
        RestAssured.port = serverPortDefault.getNumber();
        given().contentType("application/x-www-form-urlencoded")
                .header("must", "true")
                .formParam("must", "true")
                .expect()
                .response().body(containsString("no validations performed"))
                .statusCode(200)
                .when().put("/default/forgiving/one?must=true");
    }

    @Test
    public void disableValidations()
    {
        RestAssured.port = serverPortNoValidations.getNumber();
        given().contentType("application/x-www-form-urlencoded")
                .formParam("one", "true")
                .expect()
                .response().body(containsString("no validations performed"))
                .statusCode(200)
                .when().put("/no-validations/forgiving/any");
    }
}
