/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import com.jayway.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class BaseUriParamsInBasePathTestCase extends FunctionalTestCase {

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs() {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources() {
        return "org/mule/module/apikit/parameters/base-uri-parameters.xml";
    }

    @Test
    public void successWhenSendingBaseUriParams() throws Exception {
        given().expect()
                .response().body(CoreMatchers.containsString("  \"stat\":\"sucess\"\n"))
                .response().statusCode(200)
                .when().get("/api/v1.0/uriparam/resource/test");
    }
}
