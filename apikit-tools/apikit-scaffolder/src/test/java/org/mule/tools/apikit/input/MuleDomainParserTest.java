/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.mule.tools.apikit.model.HttpListenerConfig;
import java.io.InputStream;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Test;

public class MuleDomainParserTest
{
    @Test
    public void testCustomDomain() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "custom-domain/mule-domain-config.xml");
        Log log = mock(Log.class);

        MuleDomainParser muleDomainParser = new MuleDomainParser(log, resourceAsStream);
        Map<String, HttpListenerConfig> httpListenerConfigs = muleDomainParser.getHttpListenerConfigs();
        assertNotNull(httpListenerConfigs);
        assertEquals(1, httpListenerConfigs.size());
        String expectedKey = "http-lc-0.0.0.0-8081";
        HttpListenerConfig value = httpListenerConfigs.get(expectedKey);
        Assert.assertNotNull(value);
        Assert.assertEquals("http-lc-0.0.0.0-8081", value.getName());
        Assert.assertEquals("0.0.0.0", value.getHost());
        Assert.assertEquals("8081", value.getPort());
        Assert.assertEquals("/", value.getBasePath());
    }

    @Test
    public void testMultipleLCInDomain() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "custom-domain-multiple-lc/mule-domain-config.xml");
        Log log = mock(Log.class);

        MuleDomainParser muleDomainParser = new MuleDomainParser(log, resourceAsStream);
        Map<String, HttpListenerConfig> httpListenerConfigs = muleDomainParser.getHttpListenerConfigs();
        assertNotNull(httpListenerConfigs);
        assertEquals(4, httpListenerConfigs.size());

        String expectedKey = "abcd";
        HttpListenerConfig value = httpListenerConfigs.get(expectedKey);
        Assert.assertNotNull(value);
        Assert.assertEquals("abcd", value.getName());
        Assert.assertEquals("localhost", value.getHost());
        Assert.assertEquals("7001", value.getPort());
        Assert.assertEquals("/", value.getBasePath());

        String expectedKey2 = "http-lc-0.0.0.0-8083";
        HttpListenerConfig value2 = httpListenerConfigs.get(expectedKey2);
        Assert.assertNotNull(value2);
        Assert.assertEquals("http-lc-0.0.0.0-8083", value2.getName());
        Assert.assertEquals("0.0.0.0", value2.getHost());
        Assert.assertEquals("8083", value2.getPort());
        Assert.assertEquals("/test", value2.getBasePath());

        String expectedKey3 = "http-lc-0.0.0.0-8080";
        HttpListenerConfig value3 = httpListenerConfigs.get(expectedKey3);
        Assert.assertNotNull(value3);
        Assert.assertEquals("http-lc-0.0.0.0-8080", value3.getName());
        Assert.assertEquals("localhost", value3.getHost());
        Assert.assertEquals("8080", value3.getPort());
        Assert.assertEquals("/", value3.getBasePath());


        String expectedKey4 = "https-lc-0.0.0.0-8082";
        HttpListenerConfig value4 = httpListenerConfigs.get(expectedKey4);
        Assert.assertNotNull(value4);
        Assert.assertEquals("https-lc-0.0.0.0-8082", value4.getName());
        Assert.assertEquals("0.0.0.0", value4.getHost());
        Assert.assertEquals("8082", value4.getPort());
        Assert.assertEquals("/", value4.getBasePath());
    }


    @Test
    public void testEmptyDomain() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "empty-domain/mule-domain-config.xml");
        Log log = mock(Log.class);

        MuleDomainParser muleDomainParser = new MuleDomainParser(log, resourceAsStream);
        Map<String, HttpListenerConfig> httpListenerConfigs = muleDomainParser.getHttpListenerConfigs();
        assertNotNull(httpListenerConfigs);
        assertEquals(0, httpListenerConfigs.size());
    }
}
