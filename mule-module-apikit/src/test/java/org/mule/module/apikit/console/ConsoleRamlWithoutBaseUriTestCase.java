/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class ConsoleRamlWithoutBaseUriTestCase extends MuleArtifactFunctionalTestCase
{
    @Rule public DynamicPort serverPort = new DynamicPort("serverPort");

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
        return "org/mule/module/apikit/console/console-with-raml-without-baseuri.xml";
    }


    @Test
    public void getRootRamlConsoleWithoutRouterKRBUTrue()
    {
        given().port(serverPort.getNumber())
                .header("Accept", "application/raml+yaml")
                .expect()
                .header("Content-Type", "application/raml+yaml")
                .response()
                .statusCode(200)
                .body(containsString("/types-test:"))
                .body(not(containsString("baseUri:")))
                .when().get("consoleWithoutRouterKRBUTrue/org/mule/module/apikit/console/?raml");
    }

    @Test
    public void getRootRamlConsoleWithoutRouterKRBUFalse()
    {
        given().port(serverPort.getNumber())
                .header("Accept", "application/raml+yaml")
                .expect()
                .header("Content-Type", "application/raml+yaml")
                .response()
                .statusCode(200)
                .body(containsString("/types-test:"))
                .body(not(containsString("baseUri:")))
                .when().get("consoleWithoutRouterKRBUFalse/org/mule/module/apikit/console/?raml");
    }

    @Test
    public void getRootRamConsolelWithRouter()
    {
        given().port(serverPort.getNumber())
                .header("Accept", "application/raml+yaml")
                .expect()
                .header("Content-Type", "application/raml+yaml")
                .response()
                .statusCode(200)
                .body(containsString("/types-test:"))
                .body(containsString("baseUri: http://localhost"))
                .when().get("consoleWithRouter/org/mule/module/apikit/console/?raml");
    }
}
