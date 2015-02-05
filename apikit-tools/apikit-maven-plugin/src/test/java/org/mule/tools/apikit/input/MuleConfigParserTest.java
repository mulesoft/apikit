/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import junit.framework.Assert;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.input.MuleConfigParser;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

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

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, ramlPaths, streams, new APIFactory());
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
    public void testCreationOld() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntriesOld/leagues-flow-config.xml");
        Log log = mock(Log.class);

        HashSet<File> ramlPaths = new HashSet<File>();
        ramlPaths.add(new File("leagues.raml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, ramlPaths, streams, new APIFactory());
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

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, ramlPaths, streams, new APIFactory());
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

        Map<String, APIKitConfig> configs = muleConfigParser.getApikitConfigs();
        APIKitConfig leaguesConfig = configs.get("leagues-config");
        assertNotNull(leaguesConfig);
        assertEquals("leagues-config", leaguesConfig.getName());
        assertEquals("leagues.raml", leaguesConfig.getRaml());
        assertTrue(leaguesConfig.isConsoleEnabled());
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

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, ramlPaths, streams, new APIFactory());
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
        assertTrue(leaguesConfig.isConsoleEnabled());
        assertEquals(APIKitConfig.DEFAULT_CONSOLE_PATH, leaguesConfig.getConsolePath());
    }
}
