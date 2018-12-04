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

import org.junit.Ignore;

import org.mule.module.launcher.application.Application;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.deployment.AbstractFakeMuleServerTestCase;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ProxyTestCase extends AbstractFakeMuleServerTestCase
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
        muleServer.deployAppFromClasspathFolder("org/mule/module/apikit/proxy/proxied-app", "proxied");
        muleServer.start();
        muleServer.deployAppFromClasspathFolder(getProxyAppFolder(), "proxy");
        muleServer.assertDeploymentSuccess("proxy");
        getOnLeaguesJson();
        getOnLeagueJson();
        notAcceptable();
        notFound();
        methodNotAllowed();
        getWithRequiredQueryParam();
    }

    @Test
    public void proxyStartStop() throws Exception
    {
        muleServer.deployAppFromClasspathFolder("org/mule/module/apikit/proxy/proxied-app", "proxied");
        muleServer.start();
        muleServer.deployAppFromClasspathFolder(getProxyAppFolder(), "proxy");
        muleServer.assertDeploymentSuccess("proxy");
        getOnLeaguesJson();
        getOnLeagueJson();
        notAcceptable();
        notFound();
        methodNotAllowed();
        getWithRequiredQueryParam();

        Application application = muleServer.findApplication("proxied");
        application.stop();
        application.start();

        getOnLeaguesJson();
        getOnLeagueJson();
        notAcceptable();
        notFound();
        methodNotAllowed();
        getWithRequiredQueryParam();
    }

    protected String getProxyAppFolder()
    {
        return "org/mule/module/apikit/proxy/app";
    }

    private void getOnLeaguesJson() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body("name", hasItems("Liga BBVA", "Premiere League"))
                .header("Content-type", containsString("application/json")).statusCode(200)
                .when().get("/proxy/leagues");
    }

    private void getOnLeagueJson() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body("name", is("Liga BBVA"))
                .header("Content-type", containsString("application/json")).statusCode(200)
                .when().get("/proxy/leagues/1");
    }

    private void notAcceptable() throws Exception
    {
        given().header("Accept", "application/xml")
                .expect()
                .response().statusCode(406)
                .when().get("/proxy/leagues");
    }

    private void notFound() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().statusCode(404)
                .when().get("/proxy/leaguess");
    }

    private void methodNotAllowed() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().statusCode(405)
                .when().patch("/proxy/leagues");
    }

    private void getWithRequiredQueryParam()
    {
        given().header("Accept", "application/json")
                .expect()
                .response().statusCode(400).body(is("bad request"))
                .when().get("/proxy/leagues/1/teams");

        given().header("Accept", "application/json")
                .queryParam("limit", "5")
                .expect()
                .response().body("name", hasItems("Atleti", "Elche"))
                .header("Content-type", containsString("application/json")).statusCode(200)
                .when().get("/proxy/leagues/1/teams");
    }

}
