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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class FormParametersTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/parameters/form-parameters-config.xml";
    }

    @Test
    public void validMultipartFormProvided() throws Exception
    {
        given().multiPart("first", "primero")
                .multiPart("second", "segundo")
                .multiPart("third", "true")
                .multiPart("payload", "3.4")
            .expect().response().statusCode(201)
                .when().post("/api/multipart");
    }

    @Test
    public void requiredMultipartFormParamNotProvided() throws Exception
    {
        given().multiPart("second", "segundo")
                .multiPart("third", "true")
                .multiPart("payload", "3.4")
            .expect().response().statusCode(400)
                .when().post("/api/multipart");
    }

    @Test
    public void validUrlencodedFormProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
            .expect().response().statusCode(201)
                .when().post("/api/url-encoded?first=primer&second=segundo&third=true");
    }

    @Test
    public void requiredUrlencodedFormParamNotProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
            .expect().response().statusCode(400)
                .when().post("/api/url-encoded?second=segundo&third=true");
    }

    @Test
    public void invalidUrlencodedFormProvided() throws Exception
    {
        given().header("Content-Type", "application/x-www-form-urlencoded")
            .expect().response().statusCode(400)
                .when().post("/api/url-encoded?first=primer&second=segundo&third=35");
    }

}
