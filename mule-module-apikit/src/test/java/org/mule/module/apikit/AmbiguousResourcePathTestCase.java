/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;


import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class AmbiguousResourcePathTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 600000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/ambiguous/flow-config.xml";
    }

    @Test
    public void staticResourceA() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect().response().statusCode(200)
                .when().get("/api/root/resourceA");
    }

    @Test
    public void stringParamResource() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect().response().statusCode(201)
                .when().get("/api/root/resourceC");
    }

    @Test
    public void staticResourceB() throws Exception
    {
        given().header("Accept", "text/plain")
                .expect().response().statusCode(202)
                .when().get("/api/root/resourceB");
    }

}
