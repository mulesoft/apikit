package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.input.MuleConfigParser;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MuleConfigParserTest {
    @Test
    public void testCreation() {
        final InputStream resourceAsStream =
                MuleConfigParser.class.getClassLoader().getResourceAsStream(
                        "testGetEntries/leagues-flow-config.xml");
        Log log = mock(Log.class);

        HashSet<File> yamlPaths = new HashSet<File>();
        yamlPaths.add(new File("leagues.yaml"));

        HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
        streams.put(new File(""), resourceAsStream);

        MuleConfigParser muleConfigParser =
                new MuleConfigParser(log, yamlPaths, streams);
        Set<ResourceActionPair> set = muleConfigParser.getEntries();
        assertNotNull(set);
        assertEquals(5, set.size());

        Set<API> apis = muleConfigParser.getIncludedApis();
        assertNotNull(apis);
        assertEquals(1, apis.size());
        assertEquals("leagues.yaml", apis.iterator().next().getYamlFile().getName());

    }
}
