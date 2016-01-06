/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.proxy;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.deployment.AbstractFakeMuleServerTestCase;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class MultipleProxyTestCase extends AbstractFakeMuleServerTestCase
{
    private static final int PROXIES_COUNT = 2;

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
        muleServer.deployAppFromClasspathFolder("org/mule/module/apikit/proxy/proxied-app", "proxied");
        muleServer.start();
        muleServer.deployAppFromClasspathFolder("org/mule/module/apikit/proxy/app-multiple", "proxy");
        muleServer.assertDeploymentSuccess("proxy");
        for(int count = 1; count <= PROXIES_COUNT; count ++)
        {
            getOnLeaguesJson(count);
        }
    }

    private void getOnLeaguesJson(int proxy) throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body("name", hasItems("Liga BBVA", "Premiere League"))
                .header("Content-type", containsString("application/json")).statusCode(200)
                .when().get("/proxy" + proxy + "/leagues");
    }

}
