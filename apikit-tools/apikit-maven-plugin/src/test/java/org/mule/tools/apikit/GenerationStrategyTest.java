package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerationStrategyTest {

    private GenerationStrategy generationStrategy;

    @Before
    public void setUp() {
        generationStrategy = new GenerationStrategy(mock(Log.class));
    }

    @Test
    public void testAllEmptyGenerate() throws Exception {
        Set<ResourceActionPair> generate = generationStrategy.generate(mock(RAMLFilesParser.class),
                mock(MuleConfigParser.class));
        assertEquals(0, generate.size());
    }

    @Test
    public void testNotEmptyYamlGenerate() throws Exception {
        final API fromYAMLFile = API.createAPIBinding(new File("sample.yaml"), null, "http://localhost/");
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);

        when(yaml.getEntries()).thenReturn(new HashSet<ResourceActionPair>() {{
            this.add(new ResourceActionPair(fromYAMLFile, "pet", "post"));
        }});

        Set<ResourceActionPair> generate = generationStrategy.generate(yaml, mule);
        assertEquals(1, generate.size());
    }

    @Test
    public void testExistingAPIKitFlow() throws Exception {
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                API.createAPIBinding(new File("sample.yaml"), new File("sample.xml"), "/api");

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionPair>() {{
            this.add(new ResourceActionPair(api, "/pet", "GET"));
        }});

        when(yaml.getEntries()).thenReturn(new HashSet<ResourceActionPair>() {{
            this.add(new ResourceActionPair(API.createAPIBinding(
                    new File("sample.yaml"), null, "http://localhost/"),
                    "/pet", "GET"));
        }});

        Set<ResourceActionPair> generate = generationStrategy.generate(yaml, mule);
        assertEquals(0, generate.size());
    }

    @Test
    public void testNonExistingAPIKitFlow() throws Exception {
        RAMLFilesParser yaml = mock(RAMLFilesParser.class);
        MuleConfigParser mule = mock(MuleConfigParser.class);
        final API api =
                API.createAPIBinding(new File("sample.yaml"), new File("sample.xml"), "/api");

        when(mule.getIncludedApis()).thenReturn(new HashSet<API>() {{
            this.add(api);
        }});

        when(mule.getEntries()).thenReturn(new HashSet<ResourceActionPair>() {{
            this.add(new ResourceActionPair(api, "/pet", "GET"));
        }});

        when(yaml.getEntries()).thenReturn(new HashSet<ResourceActionPair>() {{
            API fromYAMLFile = API.createAPIBinding(new File("sample.yaml"), null, "http://localhost/");
            this.add(new ResourceActionPair(fromYAMLFile, "/pet", "GET"));
            this.add(new ResourceActionPair(fromYAMLFile, "/pet", "POST"));
        }});

        Set<ResourceActionPair> generate = generationStrategy.generate(yaml, mule);
        assertEquals(1, generate.size());
    }
}
