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
import org.mule.api.config.MuleProperties;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.File;

import static com.jayway.restassured.RestAssured.given;

public class SpacesInPathTestCase extends FunctionalTestCase
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

        String appHomeDirectory = getAppHomeDirectory();
        muleContext.getRegistry().registerObject(MuleProperties.APP_HOME_DIRECTORY_PROPERTY, appHomeDirectory);
        super.doSetUp();
    }

    private String getAppHomeDirectory() {
        return getClass().getResource("/").getPath();
    }


    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/console/space-in-filename.xml";
    }

    @Test
    public void successWhenRamlResourcePathContainsSpaces()
    {
        given()
            .expect()
                .statusCode(200)
                .when().get("console/org/mule/module/apikit/console/this is a test.json");
    }

}
