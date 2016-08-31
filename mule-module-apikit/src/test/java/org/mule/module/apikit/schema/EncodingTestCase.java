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

import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;

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
    public void postXmlW1252on10asStream() throws Exception
    {
        postXmlWindows1252("/api10");
    }

    @Test
    public void postXmlW1252on10asStreamNoCharset() throws Exception
    {
        postXmlWindows1252("/api10", false);
    }

    @Test
    public void postXmlW1252on10asString() throws Exception
    {
        postXmlWindows1252("/api10str");
    }

    @Test
    public void postXmlW1252on10asByteArray() throws Exception
    {
        postXmlWindows1252("/api10byte");
    }

    @Test
    public void postXmlW1252on10asByteArrayNoCharset() throws Exception
    {
        postXmlWindows1252("/api10byte", false);
    }

    @Test
    public void postXmlW1252on08asStream() throws Exception
    {
        postXmlWindows1252("/api08");
    }

    @Test
    public void postXmlW1252on08asStreamNoCharset() throws Exception
    {
        postXmlWindows1252("/api08", false);
    }

    @Test
    public void postXmlW1252on08asString() throws Exception
    {
        postXmlWindows1252("/api08str");
    }

    @Test
    public void postXmlW1252on08asByteArray() throws Exception
    {
        postXmlWindows1252("/api08byte");
    }

    @Test
    public void postXmlW1252on08asByteArrayNoCharset() throws Exception
    {
        postXmlWindows1252("/api08byte", false);
    }

    private void postXmlWindows1252(String api) throws Exception
    {
        postXmlWindows1252(api, true);
    }

    private void postXmlWindows1252(String api, boolean sendCharset) throws Exception
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/schema/payload-windows1252.xml");
        byte[] body = IOUtils.toByteArray(inputStream);
        String responseBody = IOUtils.toString(body, "windows-1252");
        String contentType = "application/xml";
        if (sendCharset)
        {
            contentType += ";charset=windows-1252";
        }
        given().body(body)
                .header("Content-Type", contentType)
                .header("Accept", "application/xml")
                .expect().log().everything().statusCode(200)
                .body(is(responseBody))
                .when().post(api + "/testXml");
    }

    @Test
    public void postJsonUtf16beOn10asStream() throws IOException
    {
        postJsonUtf16be("/api10");
    }

    @Test
    public void postJsonUtf16beOn10asStreamNoCharset() throws IOException
    {
        postJsonUtf16be("/api10", false);
    }

    @Test
    public void postJsonUtf16beOn10asString() throws IOException
    {
        postJsonUtf16be("/api10str");
    }

    @Test
    public void postJsonUtf16beOn10asByteArray() throws IOException
    {
        postJsonUtf16be("/api10byte");
    }

    @Test
    public void postJsonUtf16beOn10asByteArrayNoCharset() throws IOException
    {
        postJsonUtf16be("/api10byte", false);
    }

    @Test
    public void postJsonUtf16beOn08asStream() throws IOException
    {
        postJsonUtf16be("/api08");
    }

    @Test
    public void postJsonUtf16beOn08asStreamNoCharset() throws IOException
    {
        postJsonUtf16be("/api08", false);
    }

    @Test
    public void postJsonUtf16beOn08asString() throws IOException
    {
        postJsonUtf16be("/api08str");
    }

    @Test
    public void postJsonUtf16beOn08asByteArray() throws IOException
    {
        postJsonUtf16be("/api08byte");
    }

    @Test
    public void postJsonUtf16beOn08asByteArrayNoCharset() throws IOException
    {
        postJsonUtf16be("/api08byte", false);
    }


    private void postJsonUtf16be(String api) throws IOException
    {
        postJsonUtf16be(api, true);
    }

    private void postJsonUtf16be(String api, boolean sendCharset) throws IOException
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/unicode/diacritics-utf16be.json");
        byte[] body = IOUtils.toByteArray(inputStream);
        String contentType = "application/json";
        if (sendCharset)
        {
            contentType += ";charset=UTF-16";
        }
        String responseBody = IOUtils.toString(body, "UTF-16");
        given().body(body)
                .header("Content-Type", contentType)
                .expect()
                .statusCode(200)
                .body(is(responseBody))
                .when().post(api + "/testJson");
    }

}
