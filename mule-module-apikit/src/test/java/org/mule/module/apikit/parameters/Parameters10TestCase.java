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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class Parameters10TestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

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
        return "org/mule/module/apikit/parameters/parameters-10-config.xml";
    }

    @Test
    public void repeatableQueryParam()
    {
        given().queryParam("status", "a", "b")
                .expect().response().statusCode(200)
                .body(is("parameters: ParameterMap{[status=[a, b], default=[default value]]}"))
                .when().get("/api/repeat");
    }

    @Test
    public void failMinItemsRepeatableQueryParam()
    {
        given().queryParam("boolean-array", "true", "false")
                .expect().response().statusCode(500)
                .body(is("Expected min items 3 for boolean-array and got 2 (org.mule.module.apikit.exception.InvalidQueryParameterException)."))
                .when().get("/api/repeat");
    }

    @Test
    public void failMaxItemsRepeatableQueryParam()
    {
        given().queryParam("boolean-array", "true", "false", "false", "true", "false")
                .expect().response().statusCode(500)
                .body(is("Expected max items 4 for boolean-array and got 5 (org.mule.module.apikit.exception.InvalidQueryParameterException)."))
                .when().get("/api/repeat");
    }

    @Test
    public void successRepeatableQueryParam()
    {
        given().queryParam("boolean-array", "true", "false", "false", "true")
                .expect().response().statusCode(200)
                .when().get("/api/repeat");
    }

    @Test
    public void failMinItemsUnionRepeatableQueryParam()
    {
        given().queryParam("union-boolean-array", "true", "false")
                .expect().response().statusCode(500)
                .body(is("- For union-boolean-array one of the union component failed: Parameter union-boolean-array is not an array\n- For union-boolean-array one of the union component failed: Expected min items 3 for union-boolean-array and got 2\n (org.mule.module.apikit.exception.InvalidQueryParameterException)."))
                .when().get("/api/repeat");
    }

    @Test
    public void failMaxItemsUnionRepeatableQueryParam()
    {
        given().queryParam("union-boolean-array", "true", "false", "false", "true", "false")
                .expect().response().statusCode(500)
                .body(is("- For union-boolean-array one of the union component failed: Parameter union-boolean-array is not an array\n- For union-boolean-array one of the union component failed: Expected max items 4 for union-boolean-array and got 5\n (org.mule.module.apikit.exception.InvalidQueryParameterException)."))
                .when().get("/api/repeat");
    }

    @Test
    public void successRepeatableUnionQueryParam()
    {
        given().queryParam("union-boolean-array", "true", "false", "false", "true")
                .expect().response().statusCode(200)
                .when().get("/api/repeat");
    }

    @Test
    public void repeatableStringQueryParamWithAsterisk()
    {
        given().queryParam("status", "*a", "b")
                .expect().response().statusCode(200)
                .body(is("parameters: ParameterMap{[status=[*a, b], default=[default value]]}"))
                .when().get("/api/repeat");
    }

    @Test
    public void arrayStringQueryParamWithInteger()
    {
        given().queryParam("status", "123")
                .expect().response().statusCode(200)
                .body(is("parameters: ParameterMap{[status=[123], default=[default value]]}"))
                .when().get("/api/repeat");
    }

    @Test
    public void arrayStringQueryParamWithIntegers()
    {
        given().queryParam("status", "123", "456")
                .expect().response().statusCode(200)
                .body(is("parameters: ParameterMap{[status=[123, 456], default=[default value]]}"))
                .when().get("/api/repeat");
    }

    @Test
    public void repeatableHeader()
    {
        given().header("repeatable", "a")
                .header("repeatable", "b")
                .expect().response().statusCode(200)
                .body(is("headers: [a, b]"))
                .when().get("/api/repeatHeader");
    }

    @Test
    public void unionString()
    {
        given().queryParam("union-string", "100")
                .expect().response().statusCode(200)
                .when().get("/api/repeat");
    }

    @Test
    public void nilStringTypeNullValid()
    {
        given().queryParam("union-string", (String) null)
            .expect().response().statusCode(200)
            .when().get("/api/repeat");
    }

    @Test
    public void stringTypeNullValid()
    {
        given().queryParam("string-type", (String) null)
            .expect().response().statusCode(200)
            .when().get("/api/repeat");
    }


    @Test
    public void parameterMapAfterDefaultAdded() {
        given().queryParam("status", "a", "b")
                .expect().response().statusCode(200)
                .body(is("parameters: ParameterMap{[status=[a, b], default=[default value]]}"))
                .when().get("/api/repeat");
    }
}
