/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.proxy;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class HttpProxyTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    @Rule
    public DynamicPort proxyPort = new DynamicPort("proxyPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = proxyPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/proxy/http-proxy-config.xml";
    }

    @Test
    public void getRaml() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: \"http://localhost:" + proxyPort.getValue() + "/api\""))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/api");
    }

    @Test
    public void getConsole() throws Exception
    {
        given().redirects().follow(true).header("Accept", "text/html")
                .expect().log().everything()
                .response().body(containsString("raml-console-loader src=\"./?\""))
                .statusCode(200)
                .when().get("/console");
    }

    @Test
    public void getConsoleSlash() throws Exception
    {
        given().header("Accept", "text/html")
                .expect().log().everything()
                .response().body(containsString("raml-console-loader src=\"./?\""))
                .statusCode(200)
                .when().get("/console/");
    }

}
