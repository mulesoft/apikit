/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

public class ConfigurationRouterFlowResolverTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPortMapping = new DynamicPort("serverPortMapping");


    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/router-flow-resolver/router-flow-resolver-test-config.xml";
    }


    @Test
    public void flowsAreNotStoppedAfterRouterFlowResolverIsCalled() throws Exception
    {
        RestAssured.port = serverPortMapping.getNumber();

        for (Map.Entry<String, FlowResolver> entry :
            muleContext.getRegistry().lookupObject(Configuration.class).getRestFlowMap().entrySet())
        {
            entry.getValue().getFlow();
        }

        given()
            .expect()
            .response().body(is("explicitely mapped flow"))
            .statusCode(200)
            .when().get("/mapping/foo");

        given()
            .expect()
            .response().body(is("explicitely mapped flow"))
            .statusCode(200)
            .when().get("/mapping/bar");
    }

}
