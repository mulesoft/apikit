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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class FormParameters10TestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/parameters/form-parameters-10-config.xml";
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Test
    public void validUrlencodedFormProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("first", "prime")
                .formParam("second", "segundo")
                .formParam("third", "true")
                .expect().response().statusCode(201)
                .when().post("/api/url-encoded");
    }

    @Test
    public void requiredUrlencodedFormParamNotProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("second", "segundo")
                .formParam("third", "true")
                .expect().response().statusCode(400)
                .when().post("/api/url-encoded");
    }

    @Test
    public void invalidTypeUrlencodedFormProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("first", "prime")
                .formParam("second", "segundo")
                .formParam("third", "35")
                .expect().response().statusCode(400)
                .when().post("/api/url-encoded");
    }

    @Test
    public void invalidEnumUrlencodedFormProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("first", "prime")
                .formParam("second", "second")
                .expect().response().statusCode(400)
                .when().post("/api/url-encoded");
    }

}
