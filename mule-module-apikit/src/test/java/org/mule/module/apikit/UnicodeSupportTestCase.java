/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.raml.parser.utils.StreamUtils;

public class UnicodeSupportTestCase extends FunctionalTestCase
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
        RestAssured.config = new RestAssuredConfig().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/unicode/resource-config.xml";
    }

    @Test
    public void resource() throws Exception
    {
        given().expect().response().statusCode(200)
                .when().get("/api/pingüino");
    }

    @Test
    public void template() throws Exception
    {
        given()
                .expect().response().statusCode(200)
                .body(is("my name is frío"))
                .when().get("/api/pingüino/frío");
    }

    @Test
    public void templateEncodedSlashUnicodeUri() throws Exception
    {
        URL url = new URL(RestAssured.baseURI + ":" + serverPort.getNumber() + "/api/ping%C3%BCino/%2F");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
        String result = br.readLine();
        assertThat(result, is("my name is /"));
    }

    @Test
    public void jsonBodyUtf8() throws IOException
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/unicode/diacritics-utf8.json");
        String body = StreamUtils.toString(inputStream);
        given().body(body)
                .contentType("application/json")
                .expect().statusCode(200)
                .body(is(body))
                .when().post("/api/test");
    }

    @Test
    public void jsonBodyUtf16BE() throws IOException
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/unicode/diacritics-utf16be.json");
        String body = StreamUtils.toString(inputStream); //removes BOM
        given().body(body)
                .contentType("application/json")
                .expect().statusCode(200)
                .body(is(body))
                .when().post("/api/test");
    }
}
