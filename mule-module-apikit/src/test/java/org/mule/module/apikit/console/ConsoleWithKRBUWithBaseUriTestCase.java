/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@ArtifactClassLoaderRunnerConfig
public class ConsoleWithKRBUWithBaseUriTestCase extends MuleArtifactFunctionalTestCase
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
        return "org/mule/module/apikit/console/console-with-krbu-with-baseuri.xml";
    }


    @Test
    public void getRootRamlConsoleWithoutRouter()
    {
        given().port(serverPort.getNumber())
                .header("Accept", "application/raml+yaml")
                .expect()
                .header("Content-Type", "application/raml+yaml")
                .response()
                .statusCode(200)
                .body(containsString("/types-test:"))
                .body(containsString("baseUri: http://www.google.com"))
                .when().get("consoleWithoutRouter/org/mule/module/apikit/console/?raml");
    }


    @Test
    public void getRootRamlConsolelWithRouter()
    {
        given().port(serverPort.getNumber())
                .header("Accept", "application/raml+yaml")
                .expect()
                .header("Content-Type", "application/raml+yaml")
                .response()
                .statusCode(200)
                .body(containsString("/types-test:"))
                .body(containsString("baseUri: http://www.google.com"))
                .when().get("consoleWithRouter/org/mule/module/apikit/console/?raml");
    }
}
