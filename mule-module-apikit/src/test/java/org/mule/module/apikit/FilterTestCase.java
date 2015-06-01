/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class FilterTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/filters/filter-flow-config.xml";
    }

    @Test
    public void filterRequest() throws Exception
    {
        given().body("plastic").contentType("text/plain")
                .expect()
                .statusCode(200) //success but emtpy body
                .body(is(""))
                .when().put("/api/some");
    }

    @Test
    public void passRequest() throws Exception
    {
        given().body("toxic").contentType("text/plain")
                .expect()
                .statusCode(200)
                .body(is("not filtered"))
                .when().put("/api/some");
    }

    @Test
    public void filterThrowRequest() throws Exception
    {
        given().body("plastic").contentType("text/plain")
                .expect()
                .statusCode(406) //mapped by mule: META-INF/services/org/mule/config/http-exception-mappings.properties
                .when().put("/api/throw");
    }

    @Test
    public void passThrowRequest() throws Exception
    {
        given().body("toxic").contentType("text/plain")
                .expect()
                .statusCode(200)
                .body(is("not filtered"))
                .when().put("/api/throw");
    }

}
