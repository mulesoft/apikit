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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class DoNotFailOnJsonBodyDuplicateKeyTestCase extends FunctionalTestCase
{
    private static final String JSON_STRICT_DUPLICATE_DETECTION_PROPERTY = "yagi.json_duplicate_keys_detection";

    @Before
    public void setup()
    {
        System.setProperty(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY, "true");
    }

    @After
    public void clear()
    {
        System.clearProperty(JSON_STRICT_DUPLICATE_DETECTION_PROPERTY);
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
        return "org/mule/module/apikit/schema/request-schema-validation/mule-config.xml";
    }

    @Test
    public void requestIn0DoNotFailOnJsonWithDuplicatedKey()
    {
        given().body("{ \"size\": \"oneValue\", \"size\": \"otherValue\", \"email\": \"asas.dasd@mulesoft.com\", \"name\": \"Awesome Tshirt\", \"address1\": \"Mulesoft Inc\", \"address2\": \"GEARY STREET\", \"city\": \"SFO\", \"stateOrProvince\": \"CA\", \"postalCode\": \"94583\", \"country\": \"USA\" }")
                .contentType("application/json")
                .expect().statusCode(400)
                .response()
                .body(containsString("Duplicate field 'size'"))
                .header("Content-Type", is("application/json"))
                .when().post("/api/orderTshirt");
    }

}
