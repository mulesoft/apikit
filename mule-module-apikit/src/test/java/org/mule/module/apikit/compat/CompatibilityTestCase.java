/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.compat;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mule.module.apikit.AbstractConfiguration.APPLICATION_RAML;

import org.mule.module.apikit.util.FunctionalAppDeployTestCase;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

public class CompatibilityTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    public void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/compat/compat-config.xml";
    }

    @Test
    public void getUser() throws Exception
    {
        given().header("Accept", "application/json")
                .expect()
                .response().body("name", is("Lisa"))
                .header("Content-type", "application/json").statusCode(200)
                .when().get("/api/world/user");
    }

}
