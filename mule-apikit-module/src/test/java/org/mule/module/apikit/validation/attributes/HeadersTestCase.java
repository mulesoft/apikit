/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.attributes;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class HeadersTestCase extends MuleArtifactFunctionalTestCase
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
        return "org/mule/module/apikit/validation/headers/mule-config.xml";
    }

    @Test
    public void answer400WhenRequiredHeaderIsNotSent() throws Exception {
        given().expect().response()
                .body(is("{message: 'Bad Request'}"))
                .statusCode(400)
                .when().get("/api/datetime2616");
    }

    @Test
    public void answer400WhenRequiredHeaderIsInvalid() throws Exception {
        given().request()
                .header("X-ModifiedSince", "Invalid Date")
                .expect().response()
                .body(is("{message: 'Bad Request'}"))
                .statusCode(400)
                .when().get("/api/datetime2616");
    }

    @Test
    public void answer200WhenRequiredHeaderIsValid() throws Exception {
        given().request()
                .header("X-ModifiedSince", "Sun, 28 Feb 2016 16:41:41 GMT")
                .expect().response()
                .body(is("Sun, 28 Feb 2016 16:41:41 GMT"))
                .statusCode(200)
                .when().get("/api/datetime2616");
    }

    @Test
    public void answer200WhenOptionalHeaderIsNotSent() throws Exception {
        given().expect().response()
                .body(is(""))
                .statusCode(200)
                .when().post("/api/datetime2616");
    }

    @Test
    public void answer400WhenOptionalHeaderIsNotValid() throws Exception {
        given().request()
                .header("X-MaxRows", "Hello World")
                .expect().response()
                .body(is("{message: 'Bad Request'}"))
                .statusCode(400)
                .when().post("/api/datetime2616");
    }

    @Test
    public void answer200WhenOptionalHeaderIsValid() throws Exception {
        given().request()
                .header("X-MaxRows", "200")
                .expect().response()
                .body(is("200"))
                .statusCode(200)
                .when().post("/api/datetime2616");
    }
}