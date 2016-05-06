/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.assets;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.module.apikit.util.FunctionalAppDeployTestCase;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

public class ClasspathAssetsTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    @Rule
    public DynamicPort serverPort2 = new DynamicPort("serverPort2");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/assets/assets-config.xml";
    }

    @Override
    public void doSetUp() throws Exception
    {
        RestAssured.port = serverPort2.getNumber();
        super.doSetUp();
    }

    @Test
    public void assertRaml() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: http://localhost:" + serverPort.getValue() + "/myapi"), containsString("!include example.json"))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/assets/?raml");
    }

    @Test
    public void assertConsole() throws Exception
    {
        given().header("Accept", "text/html")
                .expect()
                .response().body(containsString("src=\"assets/?raml\""))
                .header("Content-type", "text/html").statusCode(200)
                .when().get("/");
    }

}
