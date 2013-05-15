/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.integration.swagger;

import static com.jayway.restassured.RestAssured.expect;
import org.mule.module.apikit.rest.util.JsonAssert;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;

public class SwaggerIntegrationTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/integration/swagger/functional-config.xml, org/mule/module/apikit/rest/integration/swagger/functional-flows.xml";
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Test
    public void resourceList() throws Exception
    {
        String responseBody = expect().response().statusCode(200).when().options("/api/").body().print();
        assertSwaggerDescriptor(responseBody, "swagger/integration/ProductsApiResourceList");
    }

    @Test
    public void productsApiDeclaration() throws Exception
    {
        String responseBody = expect().response().statusCode(200).when().options("/api/products").body().print();
        assertSwaggerDescriptor(responseBody, "swagger/integration/ProductsApiResourceDeclaration");
    }

    @Test
    public void developersApiDeclaration() throws Exception
    {
        String responseBody = expect().response().statusCode(200).when().options("/api/developers").body().print();
        assertSwaggerDescriptor(responseBody, "swagger/integration/DevelopersApiResourceDeclaration");
    }

    private void assertSwaggerDescriptor(String responseBody, String expectedJsonFile) throws IOException, JSONException
    {
        responseBody = responseBody.replace(":" + serverPort.getNumber() + "/", ":8000/");
        JsonAssert.compareJsonAsString(expectedJsonFile, responseBody);
    }
}
