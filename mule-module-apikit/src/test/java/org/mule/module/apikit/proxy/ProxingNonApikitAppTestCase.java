/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.proxy;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.deployment.AbstractFakeMuleServerTestCase;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ProxingNonApikitAppTestCase extends AbstractFakeMuleServerTestCase
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
    public void setUp() throws Exception
    {
        super.setUp();
        RestAssured.port = proxyPort.getNumber();
    }

    @Test
    public void proxy() throws Exception
    {
        muleServer.deployAppFromClasspathFolder("org/mule/module/apikit/proxy/proxied-non-apikit-app", "proxied");
        muleServer.start();
        muleServer.deployAppFromClasspathFolder(getProxyAppFolder(), "proxy");
        muleServer.assertDeploymentSuccess("proxy");
        handleUnsupportedMediaType();
        handleNotAcceptable();
    }

    protected String getProxyAppFolder()
    {
        return "org/mule/module/apikit/proxy/app-with-raml";
    }

    private void handleUnsupportedMediaType() throws Exception
    {
        given().header("Accept", "application/xml").body("<test>hello</test>")
                .expect()
                .response().statusCode(406)
                .when().post("/proxy/leagues");
    }

    private void handleNotAcceptable() throws Exception
    {
        given().header("Accept", "application/xml")
                .expect()
                .response().statusCode(406)
                .when().get("/proxy/leagues");
    }

}
