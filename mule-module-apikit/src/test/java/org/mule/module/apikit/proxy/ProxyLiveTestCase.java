/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.proxy;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;

import com.jayway.restassured.RestAssured;

import org.junit.Ignore;
import org.junit.Test;

public class ProxyLiveTestCase extends FunctionalTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }


    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = 8081;
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/proxy/proxy-live-config.xml";
    }

    @Test @Ignore
    public void getApis() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body(containsString("john.doe"))
                .header("Content-type", "application/json").statusCode(200)
                .when().get("/api/apis");
    }

    @Test @Ignore
    public void getApi() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body(containsString("john.doe"))
                .header("Content-type", "application/json").statusCode(200)
                .when().get("/api/apis/6");
    }

    @Test @Ignore
    public void notAcceptable() throws Exception
    {
        given().header("Accept", "application/xml")
                .expect()
                .response().statusCode(406)
                .when().get("/api/apis");
    }

    @Test @Ignore
    public void notFound() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().statusCode(404)
                .when().get("/api/apiss");
    }

    @Test @Ignore
    public void methodNotAllowed() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().statusCode(405)
                .when().patch("/api/apis");
    }

}
