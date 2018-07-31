/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class QueryStringTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/validation/query-string/mule-config.xml";
    }

    @Test
    public void validQueryString()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/simple-type?property=ARG");
    }

    @Test
    public void emptyQueryString()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/simple-type");
    }

    @Test
    public void arrayProperty()
    {
        given().queryParam("property1", "ARG", "USA")
                .queryParam("property2", "RUSIA")
                .expect().statusCode(200)
                .response()
                .when().get("/api/type-property-array");
    }

    @Test
    public void arrayPropertyOneItem()
    {
        given().queryParam("property1", "ARG")
                .queryParam("property2", "RUSIA")
                .expect().statusCode(200)
                .response()
                .when().get("/api/type-property-array");
    }

    @Test
    public void requiredProperty()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/type-property-array");
    }
    @Test
    public void validObjectProperty()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/type-property-type?property={\"firstname\": \"Lionel\", \"lastname\": \"Messi\"}");
    }

    @Test
    public void invalidObjectProperty()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/type-property-type?property={\"firstname\": \"Lionel\", \"nickname\": \"Messi\"}");
    }

    @Test
    public void defaultValueProperty() {
        given().queryParam("property", "someValue")
                .expect().statusCode(200)
                .response()
                .when().get("/api/default-value-property");
    }
}
