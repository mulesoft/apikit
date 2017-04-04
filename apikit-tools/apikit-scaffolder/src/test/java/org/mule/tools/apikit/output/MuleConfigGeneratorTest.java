/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.HttpListenerConfig;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MuleConfigGeneratorTest {

    public static final String VERSION = "v1";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGenerate() throws Exception {
        List<GenerationModel> entries = new ArrayList<GenerationModel>();

        IResource resource = mock(IResource.class);

        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");

        IAction action = mock(IAction.class);

        when(action.getType()).thenReturn(IActionType.GET);

        IAction postAction = mock(IAction.class);

        when(postAction.getType()).thenReturn(IActionType.POST);

        API api = mock(API.class);
        File raml = mock(File.class);
        when(raml.getName()).thenReturn("hello.raml");
        File file = folder.newFile("hello.xml");
        HttpListenerConfig listenerConfig = new HttpListenerConfig(HttpListenerConfig.DEFAULT_CONFIG_NAME,"localhost","8080",API.DEFAULT_BASE_PATH);
        when(api.getId()).thenReturn("hello");
        when(api.getRamlFile()).thenReturn(raml);
        when(api.getXmlFile(any(File.class))).thenReturn(file);
        when(api.getPath()).thenReturn("/api/*");
        when(api.getHttpListenerConfig()).thenReturn(listenerConfig);

        entries.addAll(Arrays.asList(new GenerationModel(api, VERSION, resource, action),
                                     new GenerationModel(api, VERSION, resource, postAction)));


        Log mock = mock(Log.class);
        MuleConfigGenerator muleConfigGenerator = new MuleConfigGenerator(mock, new File(""), entries, new HashMap<String, HttpListenerConfig>(), null, null);
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
        mule.addContent(new APIKitFlowScope(flowEntry).generate());

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
    public void blankDocumentWithoutLCInDomain() throws Exception {

        HttpListenerConfig listenerConfig = new HttpListenerConfig(HttpListenerConfig.DEFAULT_CONFIG_NAME,"localhost","8080","");
        API api = mock(API.class);
        when(api.getPath()).thenReturn("/api/*");
        when(api.getHttpListenerConfig()).thenReturn(listenerConfig);
        File raml = mock(File.class);
        when(raml.getName()).thenReturn("hello.raml");
        when(api.getRamlFile()).thenReturn(raml);
        when(api.getId()).thenReturn("hello");
        File file = folder.newFile("hello.xml");
        when(api.getXmlFile(any(File.class))).thenReturn(file);

        MuleConfigGenerator muleConfigGenerator =
                new MuleConfigGenerator(mock(Log.class), new File(""), new ArrayList<GenerationModel>(), new HashMap<String, HttpListenerConfig>(), null, null);

        Document document = muleConfigGenerator.getOrCreateDocument(new HashMap<API, Document>(), api);

        Element rootElement = document.getRootElement();
        assertEquals("mule", rootElement.getName());
        Element xmlListenerConfig = rootElement.getChildren().get(0);
        assertEquals("listener-config",xmlListenerConfig.getName());

        Element mainFlow = rootElement.getChildren().get(1);

        assertEquals("flow", mainFlow.getName());
        assertEquals("hello-main", mainFlow.getAttribute("name").getValue());
        assertEquals("httpListenerConfig", mainFlow.getChildren().get(0).getAttribute("config-ref").getValue());
        assertEquals("/api/*", mainFlow.getChildren().get(0).getAttribute("path").getValue());

        Element apikitConfig = mainFlow.getChildren().get(1);
        assertEquals(0, apikitConfig.getChildren().size());

        Element consoleFlow = rootElement.getChildren().get(2);
        assertEquals("flow", consoleFlow.getName());
        assertEquals("hello-console", consoleFlow.getAttribute("name").getValue());
        assertEquals("httpListenerConfig", consoleFlow.getChildren().get(0).getAttribute("config-ref").getValue());
        assertEquals("/console/*", consoleFlow.getChildren().get(0).getAttribute("path").getValue());
        assertEquals("console", consoleFlow.getChildren().get(1).getName());

        Element globalExceptionStrategy = rootElement.getChildren().get(3);
        assertEquals("mapping-exception-strategy", globalExceptionStrategy.getName());
        assertEquals("hello-apiKitGlobalExceptionMapping", globalExceptionStrategy.getAttribute("name").getValue());

    }

    @Test
    public void blankDocumentWithLCInDomain() throws Exception {

        HttpListenerConfig listenerConfig = new HttpListenerConfig(HttpListenerConfig.DEFAULT_CONFIG_NAME,"localhost","8080","");
        API api = mock(API.class);
        when(api.getPath()).thenReturn("/api/*");
        when(api.getHttpListenerConfig()).thenReturn(listenerConfig);
        File raml = mock(File.class);
        when(raml.getName()).thenReturn("hello.raml");
        when(api.getRamlFile()).thenReturn(raml);
        when(api.getId()).thenReturn("hello");
        File file = folder.newFile("hello.xml");
        when(api.getXmlFile(any(File.class))).thenReturn(file);


        MuleConfigGenerator muleConfigGenerator =
                new MuleConfigGenerator(mock(Log.class), new File(""), new ArrayList<GenerationModel>(), new HashMap<String, HttpListenerConfig>(), null, null);

        Document document = muleConfigGenerator.getOrCreateDocument(new HashMap<API, Document>(), api);

        Element rootElement = document.getRootElement();
        assertEquals("mule", rootElement.getName());
        Element xmlListenerConfig = rootElement.getChildren().get(0);
        assertEquals("listener-config",xmlListenerConfig.getName());

        Element mainFlow = rootElement.getChildren().get(1);

        assertEquals("flow", mainFlow.getName());
        assertEquals("hello-main", mainFlow.getAttribute("name").getValue());
        assertEquals("httpListenerConfig", mainFlow.getChildren().get(0).getAttribute("config-ref").getValue());
        assertEquals("/api/*", mainFlow.getChildren().get(0).getAttribute("path").getValue());

        Element apikitConfig = mainFlow.getChildren().get(1);
        assertEquals(0, apikitConfig.getChildren().size());

        Element consoleFlow = rootElement.getChildren().get(2);
        assertEquals("flow", consoleFlow.getName());
        assertEquals("hello-console", consoleFlow.getAttribute("name").getValue());
        assertEquals("httpListenerConfig", consoleFlow.getChildren().get(0).getAttribute("config-ref").getValue());
        assertEquals("/console/*", consoleFlow.getChildren().get(0).getAttribute("path").getValue());
        assertEquals("console", consoleFlow.getChildren().get(1).getName());

        Element globalExceptionStrategy = rootElement.getChildren().get(3);
        assertEquals("mapping-exception-strategy", globalExceptionStrategy.getName());
        assertEquals("hello-apiKitGlobalExceptionMapping", globalExceptionStrategy.getAttribute("name").getValue());

    }
}
