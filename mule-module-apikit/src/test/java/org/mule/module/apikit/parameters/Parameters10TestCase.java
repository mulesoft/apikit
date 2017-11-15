/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class Parameters10TestCase extends FunctionalTestCase
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
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/parameters/parameters-10-config.xml";
    }

    @Test
    public void repeatableQueryParam()
    {
        given().queryParam("status", "a", "b")
                .expect().response().statusCode(200)
                .body(is("status: [a, b]"))
                .when().get("/api/repeat");
    }

    @Test
    public void repeatableStringQueryParamWithAsterisk()
    {
        given().queryParam("status", "*a", "b")
                .expect().response().statusCode(200)
                .body(is("status: [*a, b]"))
                .when().get("/api/repeat");
    }

}
