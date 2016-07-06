/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.schema;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.util.IOUtils;

import com.jayway.restassured.RestAssured;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.raml.parser.utils.StreamUtils;

public class EncodingTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/schema/encoding-config.xml";
    }

    @Test
    public void postXmlWindows1252on08() throws Exception
    {
        postXmlWindows1252("/api08");
    }

    @Test
    public void postXmlWindows1252on08asString() throws Exception
    {
        postXmlWindows1252("/api08str");
    }

    private void postXmlWindows1252(String api) throws Exception
    {
        InputStream inputStrem = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/schema/payload-windows1252.xml");
        String body = IOUtils.toString(inputStrem, "windows-1252");
        given().body(body)
                .contentType("application/xml;charset=windows-1252")
                .header("Accept", "application/xml")
                .expect().log().everything().statusCode(200)
                .body(is(body))
                .when().post(api + "/testXml");
    }

    @Test
    public void postJsonUtf16beOn08() throws IOException
    {
        postJsonUtf16be("/api08");
    }

    @Test
    public void postJsonUtf16beOn08asString() throws IOException
    {
        postJsonUtf16be("/api08str");
    }

    private void postJsonUtf16be(String api) throws IOException
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/unicode/diacritics-utf16be.json");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        String responseBody = StreamUtils.toString(new ByteArrayInputStream(bytes)); //removes BOM
        given().body(bytes)
                .contentType("application/json;charset=UTF-16")
                .expect()
                .statusCode(200)
                .body(is(responseBody))
                .when().post(api + "/testJson");
    }

}
