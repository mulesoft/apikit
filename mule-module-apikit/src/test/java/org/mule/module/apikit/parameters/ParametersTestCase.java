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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

public class ParametersTestCase extends FunctionalTestCase
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
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/parameters/parameters-config.xml";
    }

    @Test
    public void requiredHeaderNotProvided() throws Exception
    {
        given()
                .expect().response().statusCode(400)
                .body(is("Required header one not specified"))
                .when().get("/api/resources?first=fi");
    }

    @Test
    public void invalidEnumHeaderProvided() throws Exception
    {
        given().header("one", "invalid")
                .expect().response().statusCode(400)
                .body(is("Invalid value 'invalid' for header one. Value must be one of [foo, bar]"))
                .when().get("/api/resources?first=fi");
    }

    @Test
    public void invalidHeaderPlaceholderProvided() throws Exception
    {
        given().header("mule-special", "dough").header("one", "foo")
                .expect().response().statusCode(400)
                .body(is("Invalid value 'dough' for header mule-{?}. Value must be one of [wow, yeah]"))
                .when().get("/api/resources?first=fi");
    }

    @Test
    public void validHeaderPlaceholderProvided() throws Exception
    {
        given().header("mule-special", "yeah").header("one", "foo")
                .expect().response().statusCode(200)
                .when().get("/api/resources?first=fi");
    }

    @Test
    public void requiredQueryParamNotProvided() throws Exception
    {
        given()
                .expect().response().statusCode(400)
                .body(is("Required query parameter first not specified"))
                .when().get("/api/resources");
    }

    @Test
    public void requiredQueryParamAndHeaderProvided() throws Exception
    {
        given().header("one", "foo")
                .expect().response().statusCode(200)
                .when().get("/api/resources?first=fi");
    }

    @Test
    public void invalidQueryParamMinLength() throws Exception
    {
        given().header("one", "foo").queryParam("first", "f")
                .expect().response().statusCode(400)
                .body(is("Invalid value 'f' for query parameter first. Value length is shorter than 2"))
                .when().get("/api/resources");
    }

    @Test
    public void invalidQueryParamMaxLength() throws Exception
    {
        given().header("one", "foo").queryParam("first", "first")
                .expect().response().statusCode(400)
                .body(is("Invalid value 'first' for query parameter first. Value length is longer than 3"))
                .when().get("/api/resources");
    }

    @Test
    public void invalidQueryParamPattern() throws Exception
    {
        given().header("one", "foo").queryParam("first", "1st")
                .expect().response().statusCode(400)
                .body(is("Invalid value '1st' for query parameter first. Value does not match pattern [^0-9]*"))
                .when().get("/api/resources");
    }

    @Test
    public void validSingleUriParamType() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(200)
                .when().get("/api/resources/4");
    }

    @Test
    public void validMultipleUriParamType() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(200)
                .when().get("/api/resources/4/one");
    }

    @Test
    public void invalidParentUriParamType() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(400)
                .body(is("Invalid value '0' for uri parameter id. Value is below the minimum 1"))
                .when().get("/api/resources/0/one");
    }

    @Test
    public void invalidUriParamType() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(400)
                .body(is("Invalid value 'a' for uri parameter id. Integer required"))
                .when().get("/api/resources/a");
    }

    @Test
    public void invalidUriParamMinimum() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(400)
                .body(is("Invalid value '0' for uri parameter id. Value is below the minimum 1"))
                .when().get("/api/resources/0");
    }

    @Test
    public void invalidUriParamMaximum() throws Exception
    {
        given().header("one", "foo").queryParam("first", "fi")
                .expect().response().statusCode(400)
                .body(is("Invalid value '10' for uri parameter id. Value is above the maximum 5"))
                .when().get("/api/resources/10");
    }

    @Test
    public void invalidBooleanQueryParamProvided() throws Exception
    {
        given()
                .expect().response().statusCode(400)
                .body(is("Invalid value 'yes' for query parameter third. Value must be one of [true, false]"))
                .when().get("/api/resources?first=fi&third=yes");
    }

    @Test
    public void repeatableQueryParam()
    {
        given().queryParam("status", "a", "b")
                .expect().response().statusCode(200)
                .when().get("/api/repeat");
    }


    @Test
    public void nonRepeatableQueryParamRepeated()
    {

        given().queryParam("orderId","1234")
                .queryParam("orderId","2345")
                .queryParam("email", "s@cisco.com")
                .expect().response().statusCode(400)
                .when().get("/api/trackOrder");
    }

    @Test
    public void repeatableHeader()
    {
        given().header("repeatable", "a")
                .header("repeatable", "b")
                .expect().response()
                .statusCode(200)
                .body(is("headers: [a, b]"))
                .when().get("/api/repeatableHeader");
    }


    @Test
    public void raml() throws Exception
    {
        given().header("Accept", "application/raml+yaml")
            .expect()
                .response().body(allOf(containsString("baseUri"),
                                       containsString("http://localhost:" + serverPort.getNumber() + "/api")))
                .statusCode(200).when().get("/api");
    }

}
