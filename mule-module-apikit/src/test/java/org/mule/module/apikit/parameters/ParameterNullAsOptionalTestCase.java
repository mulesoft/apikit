/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import static com.jayway.restassured.RestAssured.given;
import static org.mule.module.apikit.HttpRestRequest.NULLABLE_AS_OPTIONAL_PROPERTY_NAME;

public class ParameterNullAsOptionalTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Rule
    public SystemProperty systemProperty = new SystemProperty(NULLABLE_AS_OPTIONAL_PROPERTY_NAME, "true");

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
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/parameters/parameter-null-as-optional-config.xml";
    }

    @Test
    public void testNullQueryParameters() throws Exception
    {
        given().param("required", "value")
                .param("optional", (Object) null)
                .param("optionalarray", (Object) null)
                .expect().response().statusCode(200)
                .when().get("/api/resource");
    }

}