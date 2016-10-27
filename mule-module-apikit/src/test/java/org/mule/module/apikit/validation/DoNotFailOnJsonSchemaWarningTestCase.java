/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class DoNotFailOnJsonSchemaWarningTestCase extends FunctionalTestCase
{
    private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";

    @Before
    public void setup()
    {
        System.setProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY, "false");
    }

    @After
    public void clear()
    {
        System.clearProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY);
    }

    @Rule
    public DynamicPort serverPort = new DynamicPort("http.port");

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
        return "org/mule/module/apikit/validation/json-schema-date/mule-config.xml";
    }

    @Test
    public void requestIn08DoNotFailOnJsonSchemaWarning()
    {
        given().body("{ \"numberOfPeople\": 90, \"startDate\": \"2016-08-23T18:25:43-05:00\" }")
                .contentType("application/json")
                .expect().statusCode(200)
                .when().post("/api/subscription");
    }
}
