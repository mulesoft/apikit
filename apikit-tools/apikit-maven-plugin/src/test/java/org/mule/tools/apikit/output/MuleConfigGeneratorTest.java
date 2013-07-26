package org.mule.tools.apikit.output;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.mule.tools.apikit.model.API;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MuleConfigGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGenerate() throws Exception {
        Set<GenerationModel> entries = new HashSet<GenerationModel>();

        Resource resource = mock(Resource.class);

        when(resource.getUri()).thenReturn("/api/pet");

        Action action = mock(Action.class);

        when(action.getType()).thenReturn(ActionType.GET);

        Action postAction = mock(Action.class);

        when(postAction.getType()).thenReturn(ActionType.POST);

        API api = mock(API.class);
        File yaml = mock(File.class);
        when(yaml.getName()).thenReturn("hello.yaml");
        File file = folder.newFile("hello.xml");

        when(api.getYamlFile()).thenReturn(yaml);
        when(api.getXmlFile(any(File.class))).thenReturn(file);
        when(api.getBaseUri()).thenReturn("http://localhost/api");

        entries.addAll(Arrays.asList(new GenerationModel(api, resource, action),
                new GenerationModel(api, resource, postAction)));


        Log mock = mock(Log.class);
        MuleConfigGenerator muleConfigGenerator = new MuleConfigGenerator(mock, new File(""), entries);
        muleConfigGenerator.generate();

        assertTrue(file.exists());
        assertTrue(file.isFile());
        verify(mock, never()).error(any(CharSequence.class), any(Throwable.class));

        String s = IOUtils.toString(new FileInputStream(file));
        assertTrue(s.length() > 0);
    }

    @Test
    public void testGenerateFlowWithExample() throws Exception {
        GenerationModel flowEntry = mock(GenerationModel.class);
        when(flowEntry.getFlowName()).thenReturn("get:/pet");
        when(flowEntry.getContentType()).thenReturn("application/json");
        when(flowEntry.getExample()).thenReturn("{\"name\": \"John\", \"kind\": \"dog\"}");

        Document doc = new Document();
        Element mule = new Element("mule");
        doc.setContent(mule);

        new APIKitFlowScope(flowEntry, mule);

        String s = Helper.nonSpaceOutput(doc);

        Diff diff = XMLUnit.compareXML("<flow " +
                "xmlns='http://www.mulesoft.org/schema/mule/core' " +
                "name='get:/pet'>" +
                "<set-property propertyName='Content-Type' value='application/json'/>" +
                "<set-payload " +
                "value='{\"name\": \"John\", \"kind\": \"dog\"}' /></flow>", s);

        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void blankDocument() throws Exception {

        API api = mock(API.class);
        String url = "http://localhost:9876/api";
        when(api.getBaseUri()).thenReturn(url);

        File yaml = mock(File.class);
        when(yaml.getName()).thenReturn("hello.yaml");
        when(api.getYamlFile()).thenReturn(yaml);
        File file = folder.newFile("hello.xml");
        when(api.getXmlFile(any(File.class))).thenReturn(file);

        MuleConfigGenerator muleConfigGenerator =
                new MuleConfigGenerator(mock(Log.class), new File(""), new HashSet<GenerationModel>());

        Document document = muleConfigGenerator.getOrCreateDocument(new HashMap<API, Document>(), api);

        Element rootElement = document.getRootElement();
        assertEquals("mule", rootElement.getName());
        Element globalExceptionStrategy = rootElement.getChildren().get(0);

        assertEquals("mapping-exception-strategy", globalExceptionStrategy.getName());
        assertEquals("apiKitGlobalExceptionMapping", globalExceptionStrategy.getAttribute("name").getValue());

        Element mainFlow = rootElement.getChildren().get(1);

        assertEquals("flow", mainFlow.getName());
        assertEquals("main", mainFlow.getAttribute("name").getValue());
        assertEquals(url, mainFlow.getChildren().get(0).getAttribute("address").getValue());

        // TODO Validate config
        //Element restProcessor = mainFlow.getChildren().get(1);
        //assertEquals("hello.yaml", restProcessor.getAttribute("config").getValue());

    }
}
