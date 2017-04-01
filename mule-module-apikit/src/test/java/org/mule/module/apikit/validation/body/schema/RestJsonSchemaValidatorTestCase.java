/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.RamlHandler;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.v1.RestSchemaV1Validator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.message.Message;
import org.mule.service.http.api.domain.ParameterMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class RestJsonSchemaValidatorTestCase
{
    private static final String jsonSchema = "{\n" +
                                            "    \"$schema\" : \"http://json-schema.org/draft-03/schema\",\n" +
                                            "    \"title\": \"League Schema\",\n" +
                                            "    \"type\": \"object\",\n" +
                                            "    \"properties\": {\n" +
                                            "        \"id\": {\n" +
                                            "            \"type\": \"string\"\n" +
                                            "        },\n" +
                                            "        \"name\": {\n" +
                                            "            \"type\": \"string\",\n" +
                                            "            \"required\": true\n" +
                                            "        }\n" +
                                            "    }\n" +
                                            "}\n";
    private static IRaml api;
    private static IAction mockedAction;

    @BeforeClass
    public static void mockApi()
    {
        api = Mockito.mock(IRaml.class);

        Map<String, Object> compiledSchemaMap = new HashMap<>();
        Schema compiledSchema = org.raml.parser.visitor.SchemaCompiler.getInstance().compile(jsonSchema);
        compiledSchemaMap.put("scheme-json", compiledSchema);
        when(api.getCompiledSchemas()).thenReturn(compiledSchemaMap);

        Map<String, String> schemaMap = new HashMap<>();
        schemaMap.put("scheme-json",jsonSchema);
        when(api.getConsolidatedSchemas()).thenReturn(schemaMap);

        Map<String, IMimeType> body = new HashMap<>();
        IMimeType mimeType = Mockito.mock(IMimeType.class);
        when(mimeType.getType()).thenReturn("application/json");
        when(mimeType.getSchema()).thenReturn("scheme-json");
        body.put("application/json", mimeType);
        mockedAction = Mockito.mock(IAction.class);
        when(mockedAction.getBody()).thenReturn(body);
        IResource mockedResource = Mockito.mock(IResource.class);
        when(mockedResource.getAction("POST")).thenReturn(mockedAction);
        when(mockedResource.getUri()).thenReturn("/leagues");
        when(mockedAction.getResource()).thenReturn(mockedResource);
        when(mockedAction.getType()).thenReturn(IActionType.POST);
        when(api.getResource("/leagues")).thenReturn(mockedResource);
    }

    @Test
    public void validStringPayloadUsingParserV1() throws BadRequestException
    {
        Message.Builder messageBuilder = Message.builder().payload("{ \"name\": \"Major League Soccer\" }");
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);
        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }

    @Test (expected = BadRequestException.class)
    public void invalidStringPayloadUsingParserV1() throws BadRequestException
    {
        Message.Builder messageBuilder = Message.builder().payload("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>");
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);

        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }

    @Test
    public void validStreamPayloadUsingParserV1() throws BadRequestException
    {
        InputStream is = new ByteArrayInputStream("{ \"name\": \"Major League Soccer\" }".getBytes());
        Message.Builder messageBuilder = Message.builder().payload(is);
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);
        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }

    @Test (expected = BadRequestException.class)
    public void invalidStreamPayloadUsingParserV1() throws BadRequestException
    {
        InputStream is = new ByteArrayInputStream("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>".getBytes());
        Message.Builder messageBuilder = Message.builder().payload(is);
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);

        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }

    @Test
    public void validByteArrayPayloadUsingParserV1() throws BadRequestException
    {
        byte[] byteArray = "{ \"name\": \"Major League Soccer\" }".getBytes();
        Message.Builder messageBuilder = Message.builder().payload(byteArray);
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);
        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }

    @Test (expected = BadRequestException.class)
    public void invalidByteArrayPayloadUsingParserV1() throws BadRequestException
    {
        byte[] byteArray = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>".getBytes();
        Message.Builder messageBuilder = Message.builder().payload(byteArray);
        ParameterMap headers = new ParameterMap();
        headers.put("content-type", "application/json");
        HttpRequestAttributes attributes = new HttpRequestAttributes(headers, null, null, null, null, null, null, null, null, null, null, null, null);
        messageBuilder.attributes(attributes);
        Message message = messageBuilder.build();
        Configuration config = new Configuration();
        RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
        when(ramlHandler.getApi()).thenReturn(api);
        config.setRamlHandler(ramlHandler);
        RestSchemaV1Validator validator = new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), mockedAction);
        Message newMessage = validator.validate(message);
        assertTrue(newMessage != null);
    }
}
