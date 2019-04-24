/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class formParameterTestCase extends FunctionalTestCase
{
    public static final String SUCCESS_MESSAGE = "{\"Message\" : \"File upload successfully\"}";
    public static final String BAD_REQUEST = "{ \"message\": \"Bad request\" }";

    @Rule
    public DynamicPort serverPort = new DynamicPort("http.port");

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
        return "org/mule/module/apikit/validation/formParameters/api.xml";
    }

    @Test
    public void valid()
    {
        given().multiPart("metadata","123")
                .multiPart("filedata","123")
                .expect().statusCode(201)
                .body(is(SUCCESS_MESSAGE))
                .when().post("/api/upload");
    }

    @Test
    public void formParameterWithoutRequiredField()
    {
        given().multiPart("metadata","123")
                .expect().statusCode(400)
                .body(is(BAD_REQUEST))
                .when().post("/api/upload");
    }
}
