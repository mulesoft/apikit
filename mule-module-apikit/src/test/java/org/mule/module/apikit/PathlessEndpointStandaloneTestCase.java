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

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class PathlessEndpointStandaloneTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPortNoRouter = new DynamicPort("serverPortNoRouter");
    @Rule
    public DynamicPort serverPortPathless = new DynamicPort("serverPortPathless");
    @Rule
    public DynamicPort serverPortEmptyPath = new DynamicPort("serverPortEmptyPath");
    @Rule
    public DynamicPort serverPortSlashPath = new DynamicPort("serverPortSlashPath");
    @Rule
    public DynamicPort serverPortAddressSlashPath = new DynamicPort("serverPortAddressSlashPath");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/pathless/pathless-standalone-config.xml";
    }

    @Test
    public void consolePathless() throws Exception
    {
        RestAssured.port = serverPortPathless.getNumber();
        console("");
    }

    @Test
    public void ramlPathless() throws Exception
    {
        RestAssured.port = serverPortPathless.getNumber();
        raml("");
    }

    @Test
    public void baseuriPathless() throws Exception
    {
        int port = serverPortPathless.getNumber();
        RestAssured.port = port;
        baseUri("", "http://localhost:" + port);
    }

    @Test
    public void consoleEmptyPath() throws Exception
    {
        RestAssured.port = serverPortEmptyPath.getNumber();
        console("");
    }

    @Test
    public void ramlEmptyPath() throws Exception
    {
        RestAssured.port = serverPortEmptyPath.getNumber();
        raml("");
    }

    @Test
    public void baseuriEmptyPath() throws Exception
    {
        int port = serverPortEmptyPath.getNumber();
        RestAssured.port = port;
        baseUri("", "http://localhost:" + port);
    }

    @Test
    public void consoleSlashPath() throws Exception
    {
        RestAssured.port = serverPortSlashPath.getNumber();
        console("");
    }

    @Test
    public void ramlSlashPath() throws Exception
    {
        RestAssured.port = serverPortSlashPath.getNumber();
        raml("");
    }

    @Test
    public void baseuriSlashPath() throws Exception
    {
        int port = serverPortSlashPath.getNumber();
        RestAssured.port = port;
        baseUri("", "http://localhost:" + port);
    }

    @Test
    public void consoleAddressSlashPath() throws Exception
    {
        RestAssured.port = serverPortAddressSlashPath.getNumber();
        console("/console");
    }

    @Test
    public void ramlAddressSlashPath() throws Exception
    {
        RestAssured.port = serverPortAddressSlashPath.getNumber();
        raml("/console/");
    }

    @Test
    public void baseuriAddressSlashPath() throws Exception
    {
        int port = serverPortAddressSlashPath.getNumber();
        RestAssured.port = port;
        baseUri("/api", "http://localhost:" + port);
    }

    private void console(String path)
    {
        given().header("Accept", "text/html")
                .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       containsString("src=\"./?")))
                .header("Content-type", "text/html").statusCode(200)
                .when().get(path + "/index.html");
    }

    private void raml(String path)
    {
        given().header("Accept", "application/raml+yaml")
                .expect()
                .response().body(allOf(containsString("title"),
                                       containsString("Endpoint API")))
                .statusCode(200)
                .when().get(path);
    }

    private void baseUri(String path, String expectedBaseUri)
    {
        given().header("Accept", "application/raml+yaml")
                .expect()
                .response().body(containsString("baseUri: \"" + expectedBaseUri + "/api\""))
                .statusCode(200)
                .when().get(path + "/");
    }

}
