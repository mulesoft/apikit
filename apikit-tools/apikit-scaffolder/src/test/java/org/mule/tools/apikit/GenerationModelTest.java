/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.raml.interfaces.model.IActionType.DELETE;
import static org.mule.raml.interfaces.model.IActionType.GET;
import static org.mule.raml.interfaces.model.IActionType.OPTIONS;
import static org.mule.raml.interfaces.model.IActionType.POST;
import static org.mule.raml.interfaces.model.IActionType.PUT;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.output.GenerationModel;

import java.util.HashMap;

import org.junit.Test;

public class GenerationModelTest {

    public static final String VERSION = "v1";

    @Test
    public void testGetVerb() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("GET", new GenerationModel(api, VERSION, resource, action).getVerb());
    }

    @Test
    public void testGetStringFromActionType() throws Exception {
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);

        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        assertEquals("retrieve", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

        action = mock(IAction.class);
        when(action.getType()).thenReturn(PUT);
        assertEquals("create", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

        action = mock(IAction.class);
        when(action.getType()).thenReturn(POST);
        assertEquals("update", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

        action = mock(IAction.class);
        when(action.getType()).thenReturn(DELETE);
        assertEquals("delete", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());

        action = mock(IAction.class);
        when(action.getType()).thenReturn(OPTIONS);
        assertEquals("options", new GenerationModel(api, VERSION, resource, action).getStringFromActionType());
    }

    @Test
    public void testGetExample() throws Exception {
        IAction action = mock(IAction.class);
        HashMap<String, IResponse> stringResponseHashMap = new HashMap<>();
        IResponse response = mock(IResponse.class);
        HashMap<String, IMimeType> stringMimeTypeHashMap = new HashMap<>();
        IMimeType mimeType = mock(IMimeType.class);
        when(mimeType.getExample()).thenReturn("{\n\"hello\": \">world<\"\n}");
        stringMimeTypeHashMap.put("application/json", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("200", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("{\n\"hello\": \">world<\"\n}",
                new GenerationModel(api, VERSION, resource, action).getExample());
    }

    @Test
    public void testGetExample200Complex() throws Exception {
        IAction action = mock(IAction.class);
        HashMap<String, IResponse> stringResponseHashMap = new HashMap<>();
        IResponse response = mock(IResponse.class);
        HashMap<String, IMimeType> stringMimeTypeHashMap = new HashMap<>();
        IMimeType mimeType = mock(IMimeType.class);
        when(mimeType.getExample()).thenReturn("<hello>world</hello>");
        stringMimeTypeHashMap.put("application/xml", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("200", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("<hello>world</hello>",
                new GenerationModel(api, VERSION, resource, action).getExample());
    }

    @Test
    public void testGetExampleComplex() throws Exception {
        IAction action = mock(IAction.class);
        HashMap<String, IResponse> stringResponseHashMap = new HashMap<>();
        IResponse response = mock(IResponse.class);
        HashMap<String, IMimeType> stringMimeTypeHashMap = new HashMap<>();
        IMimeType mimeType = mock(IMimeType.class);
        when(mimeType.getExample()).thenReturn("<hello>world</hello>");
        stringMimeTypeHashMap.put("application/xml", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("403", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("<hello>world</hello>",
                new GenerationModel(api, VERSION, resource, action).getExample());
    }

    @Test
    public void testGetExampleNull() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals(GenerationModel.DEFAULT_TEXT, new GenerationModel(api, VERSION, resource, action).getExample());
    }

    @Test
    public void testGetMadeUpName() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("retrievePet", new GenerationModel(api, VERSION, resource, action).getName());
    }

    @Test
    public void testGetRealName() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getDisplayName()).thenReturn("Animal");
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("retrieveAnimal", new GenerationModel(api, VERSION, resource, action).getName());
    }

    @Test
    public void testGetMadeUpNameWithMimeTypes() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(POST);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        GenerationModel model1 = new GenerationModel(api, VERSION, resource, action, "text/xml");
        GenerationModel model2 = new GenerationModel(api, VERSION, resource, action, "application/json");
        assertTrue(model1.compareTo(model2) != 0);
        assertEquals("updatePetTextXml", model1.getName());
        assertEquals("updatePetApplicationJson", model2.getName());
    }

    @Test
    public void testGetRelativeURI() throws Exception {
        IAction action = mock(IAction.class);
        when(action.getType()).thenReturn(GET);
        IResource resource = mock(IResource.class);
        when(resource.getResolvedUri(anyString())).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("/pet", new GenerationModel(api, VERSION, resource, action).getRelativeURI());
    }
}
