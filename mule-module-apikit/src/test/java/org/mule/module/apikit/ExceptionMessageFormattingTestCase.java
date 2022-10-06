/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class ExceptionMessageFormattingTestCase extends FunctionalTestCase {

    private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";

    @Rule
    public DynamicPort serverPort = new DynamicPort("http.port");

    @Before
    public void setup() {
        System.setProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY, "true");
    }

    @After
    public void clear() {
        System.clearProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY);
    }

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
        return "org/mule/module/apikit/exception/exception-message-formatting-config.xml";
    }

    @Test
    public void testRawJsonExceptionMessageIsNotFormatted() {
        given().body("{}")
                .contentType("application/json")
                .expect().statusCode(400)
                .response()
                .body(containsString("{\\\"errors\\\": [{\\\"message\\\": \\\"Invalid value for \\\"field\\\", required: [\\\"field\\\"].\\\"}]}"))
                .header("Content-Type", is("application/json"))
                .when().post("/api/someJsonError");
    }

    @Test
    public void testBodyValidatorExceptionMessageDoesNotEscapeMultipleTimes() {
        given().body("{ \"non-existing\": \"test\" }")
                .contentType("application/json")
                .expect().statusCode(400)
                .response()
                .body(containsString("{\"error\":\"Error validating JSON. Error: - Missing required field \\\"idNumber\\\"\\n- Missing required field \\\"firstName\\\"\"}"))
                .header("Content-Type", is("application/json"))
                .when().post("/api/bodyJson");
    }

    @Test
    public void testJsonSchemaValidatorExceptionMessageDoesNotEscapeMultipleTimes() {
        given().body("{ \"numberOfPeople\": 90, \"startDate\": \"2016-08-23T18:25:43-05:00\" }")
                .contentType("application/json")
                .expect().statusCode(400)
                .response()
                .body(containsString("format attribute \\\"date\\\" not supported"))
                .header("Content-Type", is("application/json"))
                .when().post("/api/schemaJson");
    }

}
