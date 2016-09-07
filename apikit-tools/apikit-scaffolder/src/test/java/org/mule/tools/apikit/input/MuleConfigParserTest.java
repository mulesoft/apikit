/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.jdom2.JDOMException;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

public class MuleConfigParserTest {
    @Test
    public void testCreation() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);
        Map<String, HttpListenerConfig> domainHttpListenerConfigs = new HashMap<>();
        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainHttpListenerConfigs)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("/", api.getHttpListenerConfig().getBasePath());
        assertEquals("localhost", api.getHttpListenerConfig().getHost());
        assertEquals("${serverPort}", api.getHttpListenerConfig().getPort());
        assertEquals("HTTP_Listener_Configuration", api.getHttpListenerConfig().getName());
        assertEquals("/api/*", api.getPath());
    }

    @Test
    public void testParseMultipleXmls() throws JDOMException, IOException
    {
        final InputStream xmlWithFlows =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");

        final InputStream xmlWithoutFlows =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-without-flows.xml");

        File fileWithFlows = new File("leagues.xml");
        File fileWithoutFlows = new File("api.xml");

        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));
        ramlPaths.add(new File("api.raml"));

        MuleConfigParser muleConfigParser = new MuleConfigParser(log, new APIFactory());
        muleConfigParser.parseMuleConfigFile(fileWithFlows, xmlWithFlows, ramlPaths);
        muleConfigParser.parseMuleConfigFile(fileWithoutFlows, xmlWithoutFlows, ramlPaths);

        assertEquals(6, muleConfigParser.getEntries().size());
        assertEquals(2, muleConfigParser.getApikitConfigs().size());
        assertEquals(2, muleConfigParser.getIncludedApis().size());
    }

    @Test
    public void testParseMultipleXmlsWithoutApikitDependency() throws JDOMException, IOException
    {
        File fileWithFlows = new File("leagues.xml");
        File fileWithoutFlows = new File("api.xml");

        final InputStream xmlWithFlows =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");

        final InputStream xmlWithoutApikitDependency =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/file-without-apikit-dependency.xml");

        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));
        ramlPaths.add(new File("api.raml"));

        MuleConfigParser muleConfigParser = new MuleConfigParser(log, new APIFactory());
        muleConfigParser.parseMuleConfigFile(fileWithFlows, xmlWithFlows, ramlPaths);
        muleConfigParser.parseMuleConfigFile(fileWithoutFlows, xmlWithoutApikitDependency, ramlPaths);

        assertEquals(6, muleConfigParser.getEntries().size());
        assertEquals(1, muleConfigParser.getApikitConfigs().size());
        assertEquals(1, muleConfigParser.getIncludedApis().size());
    }

    @Test
    public void testParseMultipleXmlsWithFlows() throws JDOMException, IOException
    {
        File fileWithFlows = new File("leagues.xml");
        File fileWithoutFlows = new File("simple.xml");

        final InputStream xmlWithFlows =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");

        final InputStream xmlWithoutApikitDependency =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/simple.xml");

        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));
        ramlPaths.add(new File("simple.raml"));

        MuleConfigParser muleConfigParser = new MuleConfigParser(log, new APIFactory());
        muleConfigParser.parseMuleConfigFile(fileWithFlows, xmlWithFlows, ramlPaths);
        muleConfigParser.parseMuleConfigFile(fileWithoutFlows, xmlWithoutApikitDependency, ramlPaths);

        assertEquals(8, muleConfigParser.getEntries().size());
        assertEquals(2, muleConfigParser.getApikitConfigs().size());
        assertEquals(2, muleConfigParser.getIncludedApis().size());
    }

    @Test
    public void testCreationWithEmptyDomainList() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);
        HashMap<String, HttpListenerConfig> domainStreams = new HashMap<>();

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainStreams)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("HTTP_Listener_Configuration", api.getHttpListenerConfig().getName());
        assertEquals("/api/*", api.getPath());
    }

    @Test
    public void testCreationWithDomainList() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-with-custom-lc-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);
        HashMap<String, HttpListenerConfig> domainStreams = new HashMap<>();
        HttpListenerConfig httpListenerConfig = new HttpListenerConfig("http-lc-0.0.0.0-8081", "0.0.0.0", "8081", "/");
        domainStreams.put("http-lc-0.0.0.0-8081", httpListenerConfig);
        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainStreams)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("http-lc-0.0.0.0-8081", api.getHttpListenerConfig().getName());
        assertEquals("/api/*", api.getPath());
    }

    @Test
    public void testCreationWithCustomAndNormalLC() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-with-custom-and-normal-lc-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        HashMap<String, HttpListenerConfig> domainStreams = new HashMap<>();
        HttpListenerConfig httpListenerConfig = new HttpListenerConfig("http-lc-0.0.0.0-8081", "0.0.0.0", "8081", "/");
        domainStreams.put("http-lc-0.0.0.0-8081", httpListenerConfig);

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainStreams)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("http-lc-0.0.0.0-8081", api.getHttpListenerConfig().getName());
        assertEquals("/api/*", api.getPath());
    }

    @Test
    public void testCreationOld() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntriesOld/leagues-flow-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        Map<String, HttpListenerConfig> domainHttpListenerConfigs = new HashMap<>();
        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainHttpListenerConfigs)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNull(api.getHttpListenerConfig());
        assertEquals("/api", api.getPath());
    }

    @Test
    public void testCreationWithConfigRef() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-with-config-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        Map<String, HttpListenerConfig> domainHttpListenerConfigs = new HashMap<>();
        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainHttpListenerConfigs)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("HTTP_Listener_Configuration", api.getHttpListenerConfig().getName());
        assertEquals("localhost", api.getHttpListenerConfig().getHost());
        assertEquals("${serverPort}", api.getHttpListenerConfig().getPort());
        assertEquals("/", api.getHttpListenerConfig().getBasePath());
        assertEquals("/api/*", api.getPath());

        Map<String, APIKitConfig> configs = muleConfigParser.getApikitConfigs();
        APIKitConfig leaguesConfig = configs.get("leagues-config");
        assertNotNull(leaguesConfig);
        assertEquals("leagues-config", leaguesConfig.getName());
        assertEquals("leagues.raml", leaguesConfig.getRaml());
        assertFalse(leaguesConfig.isConsoleEnabled());
        assertEquals(APIKitConfig.DEFAULT_CONSOLE_PATH, leaguesConfig.getConsolePath());
    }

    @Test
    public void testCreationWithConfigRefAndCustomLC() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-with-config-and-custom-lc-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        HashMap<String, HttpListenerConfig> domainStreams = new HashMap<>();
        HttpListenerConfig httpListenerConfig = new HttpListenerConfig("http-lc-0.0.0.0-8081", "0.0.0.0", "8081", "/");
        domainStreams.put("http-lc-0.0.0.0-8081", httpListenerConfig);

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainStreams)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        API api = apis.iterator().next();
        assertEquals("leagues.raml", api.getRamlFile().getName());
        assertEquals("leagues", api.getId());
        assertNotNull(api.getHttpListenerConfig());
        assertEquals("http-lc-0.0.0.0-8081", api.getHttpListenerConfig().getName());
        assertEquals("/api/*", api.getPath());

        Map<String, APIKitConfig> configs = muleConfigParser.getApikitConfigs();
        APIKitConfig leaguesConfig = configs.get("leagues-config");
        assertNotNull(leaguesConfig);
        assertEquals("leagues-config", leaguesConfig.getName());
        assertEquals("leagues.raml", leaguesConfig.getRaml());
        assertFalse(leaguesConfig.isConsoleEnabled());
        assertEquals(APIKitConfig.DEFAULT_CONSOLE_PATH, leaguesConfig.getConsolePath());
    }

    @Test
    public void testCreationOldWithConfigRef() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntriesOld/leagues-flow-with-config-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        Map<String, HttpListenerConfig> domainHttpListenerConfigs = new HashMap<>();
        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, new APIFactory(domainHttpListenerConfigs)).parse(ramlPaths, streams);
        Set<ResourceActionMimeTypeTriplet> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(6, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        assertEquals("leagues.raml", apis.iterator().next().getRamlFile().getName());

        Map<String, APIKitConfig> configs = muleConfigParser.getApikitConfigs();
        APIKitConfig leaguesConfig = configs.get("leagues-config");
        assertNotNull(leaguesConfig);
        assertEquals("leagues-config", leaguesConfig.getName());
        assertEquals("leagues.raml", leaguesConfig.getRaml());
        assertFalse(leaguesConfig.isConsoleEnabled());
        assertEquals(APIKitConfig.DEFAULT_CONSOLE_PATH, leaguesConfig.getConsolePath());
    }
}
