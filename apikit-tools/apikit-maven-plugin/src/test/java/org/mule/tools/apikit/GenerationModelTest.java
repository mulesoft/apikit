/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;


import org.junit.Test;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.model.API;
import org.raml.model.*;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerationModelTest {

    @Test
    public void testGetVerb() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("GET", new GenerationModel(api, resource, action).getVerb());
    }

    @Test
    public void testGetStringFromActionType() throws Exception {
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);

        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        assertEquals("retrieve", new GenerationModel(api, resource, action).getStringFromActionType());

        action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.PUT);
        assertEquals("create", new GenerationModel(api, resource, action).getStringFromActionType());

        action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.POST);
        assertEquals("update", new GenerationModel(api, resource, action).getStringFromActionType());

        action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.DELETE);
        assertEquals("delete", new GenerationModel(api, resource, action).getStringFromActionType());

        action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.OPTIONS);
        assertEquals("options", new GenerationModel(api, resource, action).getStringFromActionType());
    }

    @Test
    public void testGetExample() throws Exception {
        Action action = mock(Action.class);
        HashMap<String, Response> stringResponseHashMap = new HashMap<String, Response>();
        Response response = mock(Response.class);
        HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<String, MimeType>();
        MimeType mimeType = mock(MimeType.class);
        when(mimeType.getExample()).thenReturn("{\n\"hello\": \">world<\"\n}");
        stringMimeTypeHashMap.put("application/json", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("200", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("{\n\"hello\": \">world<\"\n}",
                new GenerationModel(api, resource, action).getExample());
    }

    @Test
    public void testGetExample200Complex() throws Exception {
        Action action = mock(Action.class);
        HashMap<String, Response> stringResponseHashMap = new HashMap<String, Response>();
        Response response = mock(Response.class);
        HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<String, MimeType>();
        MimeType mimeType = mock(MimeType.class);
        when(mimeType.getExample()).thenReturn("<hello>world</hello>");
        stringMimeTypeHashMap.put("application/xml", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("200", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("<hello>world</hello>",
                new GenerationModel(api, resource, action).getExample());
    }

    @Test
    public void testGetExampleComplex() throws Exception {
        Action action = mock(Action.class);
        HashMap<String, Response> stringResponseHashMap = new HashMap<String, Response>();
        Response response = mock(Response.class);
        HashMap<String, MimeType> stringMimeTypeHashMap = new HashMap<String, MimeType>();
        MimeType mimeType = mock(MimeType.class);
        when(mimeType.getExample()).thenReturn("<hello>world</hello>");
        stringMimeTypeHashMap.put("application/xml", mimeType);
        when(response.getBody()).thenReturn(stringMimeTypeHashMap);
        stringResponseHashMap.put("403", response);
        when(action.getResponses()).thenReturn(stringResponseHashMap);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("<hello>world</hello>",
                new GenerationModel(api, resource, action).getExample());
    }

    @Test
    public void testGetExampleNull() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals(GenerationModel.DEFAULT_TEXT, new GenerationModel(api, resource, action).getExample());
    }

    @Test
    public void testGetMadeUpName() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("retrievePet", new GenerationModel(api, resource, action).getName());
    }

    @Test
    public void testGetRealName() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getDisplayName()).thenReturn("Animal");
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("retrieveAnimal", new GenerationModel(api, resource, action).getName());
    }

    @Test
    public void testGetMadeUpNameWithMimeTypes() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.POST);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        GenerationModel model1 = new GenerationModel(api, resource, action, "text/xml");
        GenerationModel model2 = new GenerationModel(api, resource, action, "application/json");
        assertTrue(model1.compareTo(model2) != 0);
        assertEquals("updatePetTextXml", model1.getName());
        assertEquals("updatePetApplicationJson", model2.getName());
    }

    @Test
    public void testGetRelativeURI() throws Exception {
        Action action = mock(Action.class);
        when(action.getType()).thenReturn(ActionType.GET);
        Resource resource = mock(Resource.class);
        when(resource.getUri()).thenReturn("/api/pet");
        API api = mock(API.class);
        assertEquals("/pet", new GenerationModel(api, resource, action).getRelativeURI());
    }
}
