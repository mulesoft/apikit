/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class RootResourceTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/resource/root-resource-config.xml";
    }

    @Test
    public void getRootResource() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("root"))
                .header("Content-type", "text/plain").statusCode(200)
                .when().get("/api");
    }

    @Test
    public void getRootResourceWithSlash() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("root"))
                .header("Content-type", "text/plain").statusCode(200)
                .when().get("/api/");
    }

    @Test
    public void getResourceUnderRoot() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("under"))
                .header("Content-type", "text/plain").statusCode(200)
                .when().get("/api//under");
    }

    @Test
    public void getResourceUnderRootSingleSlashNotFound() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("not found"))
                .header("Content-type", "text/plain").statusCode(404)
                .when().get("/api/under");
    }

    @Test
    public void getLevelRootResource() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("level"))
                .header("Content-type", "text/plain").statusCode(200)
                .when().get("/api/level");
    }

    @Test
    public void getFlatChildResource() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect()
                .response().body(containsString("flat-child"))
                .header("Content-type", "text/plain").statusCode(200)
                .when().get("/api/flat/child");
    }


}
