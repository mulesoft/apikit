/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import com.jayway.restassured.RestAssured;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class QueryParamsTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/validation/query-params/query-params.xml";
    }

    @Test
    public void validIntegerQueryParam()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/integer?param=1");
    }

    @Test
    public void invalidIntegerQueryParam()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/integer-array?param=Hola mundo");
    }

    @Test
    public void validIntegerQueryParamArray()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/integer-array?param=[1,2]");
    }

    @Test
    public void InvalidIntegerQueryParamArray()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/integer-array?param=[Hola,2]");
    }

    @Test
    public void validIntegerQueryParamArrayAsPairs()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/integer-array?param=1&param=2");
    }

    @Test
    public void validIntegerQueryParamArrayOnlyOneValue()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/integer-array?param=1");
    }

    @Test
    public void validStringQueryParam()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/string?param=Hola mundo");
    }

    @Test
    public void validStringQueryParamArray()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/string-array?param=['Hola','mundo']");
    }

    @Test
    public void validStringQueryParamArrayAsPairs()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/string-array?param='Hola'&param='mundo'");
    }

    @Test
    public void validStringQueryParamArrayOnlyOneValue()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/string-array?param='Hola mundo'");
    }

    @Test
    public void validObjectQueryParam()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/object?param={\"name\":\"Marcelo\",\"surname\":\"Gallardo\"}");
    }

    @Test
    public void invalidObjectQueryParam()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/object?param=[{\"name\":\"Marcelo\",\"surname\":\"Gallardo\"}]");
    }

    @Test
    public void validObjectQueryParamArray()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/object-array?param=[{\"name\":\"Marcelo\",\"surname\":\"Gallardo\"}," +
                "{\"name\":\"Rafael\",\"surname\":\"Borre\"}]");
    }

    @Test
    public void validObjectQueryParamArrayAsPairs()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/object-array?param={\"name\":\"Marcelo\",\"surname\":\"Gallardo\"}" +
                "&param={\"name\":\"Rafael\",\"surname\":\"Borre\"}");
    }

    @Test
    public void validObjectQueryParamArrayOnlyOneValue()
    {
        given().expect().statusCode(200)
                .response()
                .when().get("/api/object-array?param={\"name\":\"Marcelo\",\"surname\":\"Gallardo\"}");
    }

    @Test
    public void invalidObjectQueryParamArrayOnlyOneValue()
    {
        given().expect().statusCode(400)
                .response()
                .when().get("/api/object-array?param={\"firstname\":\"Marcelo\",\"lastname\":\"Gallardo\"}");
    }

}
