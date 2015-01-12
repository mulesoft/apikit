/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerationStrategyTest {

    private GenerationStrategy generationStrategy;
    private APIFactory apiFactory;

    @Before
    public void setUp() {
        generationStrategy = new GenerationStrategy(mock(Log.class));
        apiFactory = new APIFactory();
    }

    @Test
    public void testAllEmptyGenerate() throws Exception {
        List<GenerationModel> generate = generationStrategy.generate(mock(RAMLFilesParser.class),
                mock(MuleConfigParser.class));
        assertEquals(0, generate.size());
    }

    @Test
    public void testNotEmptyYamlGenerate() throws Exception {
        final API fromYAMLFile = apiFactory.createAPIBinding(new File("sample.yaml"), null, null, new HttpListenerConfig.Builder(HttpListenerConfig.DEFAULT_CONFIG_NAME, "localhost", "80","").build(), "");
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);

        final Map<ResourceActionMimeTypeTriplet, GenerationModel> yamlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        yamlEntries.put(new ResourceActionMimeTypeTriplet(fromYAMLFile, "pet", "post"), mock(GenerationModel.class));

        when(yaml.getEntries()).thenReturn(yamlEntries);

        List<GenerationModel> generate = generationStrategy.generate(yaml, mule);
        assertEquals(1, generate.size());
    }

    @Test
    public void testExistingAPIKitFlow() throws Exception {
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                apiFactory.createAPIBinding(new File("sample.yaml"), new File("sample.xml"), null, new HttpListenerConfig.Builder().build(),"/api/*");

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionMimeTypeTriplet>() {{
            this.add(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
        }});

        final Map<ResourceActionMimeTypeTriplet, GenerationModel> yamlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        yamlEntries.put(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"), mock(GenerationModel.class));

        when(yaml.getEntries()).thenReturn(yamlEntries);

        List<GenerationModel> generate = generationStrategy.generate(yaml, mule);
        assertEquals(0, generate.size());
    }

    @Test
    public void testNonExistingAPIKitFlow() throws Exception {
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                apiFactory.createAPIBinding(new File("sample.yaml"), new File("sample.xml"), null, new HttpListenerConfig.Builder().build(),"/api/*");

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionMimeTypeTriplet>() {{
            this.add(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
        }});
        API fromYAMLFile = apiFactory.createAPIBinding(new File("sample.yaml"), null, null, new HttpListenerConfig.Builder(HttpListenerConfig.DEFAULT_CONFIG_NAME, "localhost", "80","").build(), "");
        final Map<ResourceActionMimeTypeTriplet, GenerationModel> yamlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        yamlEntries.put(new ResourceActionMimeTypeTriplet(fromYAMLFile, "/pet", "GET"), mock(GenerationModel.class));
        yamlEntries.put(new ResourceActionMimeTypeTriplet(fromYAMLFile, "/pet", "POST"), mock(GenerationModel.class));

        when(yaml.getEntries()).thenReturn(yamlEntries);

        List<GenerationModel> generate = generationStrategy.generate(yaml, mule);
        assertEquals(1, generate.size());
    }
}
