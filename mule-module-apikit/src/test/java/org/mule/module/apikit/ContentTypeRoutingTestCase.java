/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ContentTypeRoutingTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/contenttype/content-routing-flow-config.xml";
    }

    @Test
    public void postOnLeaguesJson() throws Exception
    {
        given().body("{ \"name\": \"Major League Soccer\" }")
                .contentType("application/json")
                .expect().statusCode(201)
                .header("incoming-content-type", "application/json")
                .body(is("")).header("Content-Length", "0")
                .when().post("/api/leagues");
    }

    @Test
    public void postOnLeaguesXml() throws Exception
    {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>")
                .contentType("application/xml")
                .expect().statusCode(201)
                .header("incoming-content-type", "application/xml")
                .when().post("/api/leagues");
    }

    @Test
    public void putOnSingleLeagueJson() throws Exception
    {
        given().body("{ \"name\": \"Liga Hispanica\" }")
                .contentType("application/json")
                .expect()
                .statusCode(204).body(is(""))
                .header("incoming-content-type", startsWith("application/json"))
                .when().put("/api/leagues/liga-bbva");
    }

    @Test
    public void putOnSingleLeagueXml() throws Exception
    {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>Hispanic League</name></league>")
                .contentType("application/xml")
                .expect()
                .statusCode(204).body(is(""))
                .header("incoming-content-type", startsWith("application/xml"))
                .when().put("/api/leagues/liga-bbva");
    }

    @Test
    public void postOnLeaguesInvalidMediaTypeWithOtherMediaTypeDefinedInFlow() throws Exception
    {
        given().body("<test>asdf</test>")
                .contentType("application/xml")
                .expect().statusCode(415)
                .when().post("/api/leagues2");
    }

    @Test
    public void postOnLeaguesInvalidMediaTypeWithNoMediaTypeDefinedInFlow() throws Exception
    {
        given().body("<test>asdf</test>")
                .contentType("application/xml")
                .expect().statusCode(415)
                .when().post("/api/leagues3");
    }
}
