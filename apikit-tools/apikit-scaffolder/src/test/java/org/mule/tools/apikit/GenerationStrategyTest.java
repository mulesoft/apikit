/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

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
    public void testNotEmptyRamlGenerate() throws Exception {
        final API fromRAMLFile = apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        RAMLFilesParser raml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);

        final Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "pet", "post"), mock(GenerationModel.class));

        when(raml.getEntries()).thenReturn(ramlEntries);

        List<GenerationModel> generate = generationStrategy.generate(raml, mule);
        assertEquals(1, generate.size());
    }

    @Test
    public void testExistingAPIKitFlow() throws Exception {
        RAMLFilesParser raml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                apiFactory.createAPIBinding(new File("sample.raml"), new File("sample.xml"), "http://localhost:8080", "/api/*",  null);

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionMimeTypeTriplet>() {{
            this.add(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
        }});

        final Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ramlEntries.put(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"), mock(GenerationModel.class));

        when(raml.getEntries()).thenReturn(ramlEntries);

        List<GenerationModel> generate = generationStrategy.generate(raml, mule);
        assertEquals(0, generate.size());
    }

    @Test
    public void testNonExistingAPIKitFlow() throws Exception {
        RAMLFilesParser raml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionMimeTypeTriplet>() {{
            this.add(new ResourceActionMimeTypeTriplet(api, "/pet", "GET"));
        }});
        API fromRAMLFile = apiFactory.createAPIBinding(new File("sample.raml"), null, "http://localhost:8080", "/api/*",  null);

        final Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlEntries = new HashMap<ResourceActionMimeTypeTriplet, GenerationModel>();
        ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "/pet", "GET"), mock(GenerationModel.class));
        ramlEntries.put(new ResourceActionMimeTypeTriplet(fromRAMLFile, "/pet", "POST"), mock(GenerationModel.class));

        when(raml.getEntries()).thenReturn(ramlEntries);

        List<GenerationModel> generate = generationStrategy.generate(raml, mule);
        assertEquals(1, generate.size());
    }
}
