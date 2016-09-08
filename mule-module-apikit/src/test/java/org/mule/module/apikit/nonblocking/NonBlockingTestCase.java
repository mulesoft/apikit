/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.nonblocking;


import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class NonBlockingTestCase  extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    @Rule
    public DynamicPort proxyPort = new DynamicPort("proxyPort");

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = proxyPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/non-blocking/config.xml";
    }

    @Test
    public void nonBlocking() throws Exception
    {
        given().header("Content-Type", "application/json")
                .body("{}")
                .expect()
                .response()
                .statusCode(200)
                .header("non-blocking","true")
                .when().post("/proxy/assets");
    }
}