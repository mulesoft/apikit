/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class RequestSchemaValidationTestCase extends FunctionalTestCase
{
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
        return "org/mule/module/apikit/schema/request-schema-validation/mule-config.xml";
    }

    @Test
    public void requestIn08()
    {
        given().body("{ \"size\": 1, \"email\": \"asas.dasd@mulesoft.com\", \"name\": \"Awesome Tshirt\", \"address1\": \"Mulesoft Inc\", \"address2\": \"GEARY STREET\", \"city\": \"SFO\", \"stateOrProvince\": \"CA\", \"postalCode\": \"94583\", \"country\": \"USA\" }")
                .contentType("application/json")
                .expect().statusCode(400)
                    .response()
                        .body(containsString("/size"))
                        .header("Content-Type", is("application/json"))
                .when().post("/api/orderTshirt");
    }

    @Test
    public void showAllSchemaValidationErrors()
    {
        given().body("{ \"size\": 90 }")
                .contentType("application/json")
                .expect().statusCode(400)
                .response()
                .body(containsString("{\"error\":\"error: object has missing required properties ([\\\"address1\\\",\\\"address2\\\",\\\"city\\\",\\\"country\\\",\\\"email\\\",\\\"name\\\",\\\"postalCode\\\",\\\"stateOrProvince\\\"])\\n    level: \\\"error\\\"\\n    schema: {\\\"loadingURI\\\":\\\"#\\\",\\\"pointer\\\":\\\"\\\"}\\n    instance: {\\\"pointer\\\":\\\"\\\"}\\n    domain: \\\"validation\\\"\\n    keyword: \\\"properties\\\"\\n    required: [\\\"address1\\\",\\\"address2\\\",\\\"city\\\",\\\"country\\\",\\\"email\\\",\\\"name\\\",\\\"postalCode\\\",\\\"size\\\",\\\"stateOrProvince\\\"]\\n    missing: [\\\"address1\\\",\\\"address2\\\",\\\"city\\\",\\\"country\\\",\\\"email\\\",\\\"name\\\",\\\"postalCode\\\",\\\"stateOrProvince\\\"]\\n\\nerror: instance type (integer) does not match any allowed primitive type (allowed: [\\\"string\\\"])\\n    level: \\\"error\\\"\\n    schema: {\\\"loadingURI\\\":\\\"#\\\",\\\"pointer\\\":\\\"\\/properties\\/size\\\"}\\n    instance: {\\\"pointer\\\":\\\"\\/size\\\"}\\n    domain: \\\"validation\\\"\\n    keyword: \\\"type\\\"\\n    found: \\\"integer\\\"\\n    expected: [\\\"string\\\"]\\n\\n\"}"))
                .header("Content-Type", is("application/json"))
                .when().post("/api/orderTshirt");
    }
}
