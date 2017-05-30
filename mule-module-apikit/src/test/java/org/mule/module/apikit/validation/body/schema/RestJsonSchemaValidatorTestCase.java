/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.RamlHandler;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.v1.RestJsonSchemaValidator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.core.exception.TypedException;

public class RestJsonSchemaValidatorTestCase {

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
  public static void mockApi() {
    api = Mockito.mock(IRaml.class);

    IMimeType mimeType = Mockito.mock(IMimeType.class);
    Map<String, Object> compiledSchemaMap = new HashMap<>();
    Map<String, IMimeType> body = new HashMap<>();
    Schema compiledSchema = org.raml.parser.visitor.SchemaCompiler.getInstance().compile(jsonSchema);

    compiledSchemaMap.put("scheme-json", compiledSchema);
    when(api.getCompiledSchemas()).thenReturn(compiledSchemaMap);

    Map<String, String> schemaMap = new HashMap<>();
    schemaMap.put("scheme-json", jsonSchema);
    when(api.getConsolidatedSchemas()).thenReturn(schemaMap);

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
  public void validStringPayloadUsingParser() throws TypedException, ExecutionException, BadRequestException {

    String payload = "{ \"name\": \"Major League Soccer\" }";
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);

    Configuration config = new Configuration();
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestJsonSchemaValidator JsonSchemavalidator =
        new RestJsonSchemaValidator(config.getJsonSchema("/leagues,POST,application/json").getSchema());

    JsonSchemavalidator.validate(payload);
}

  @Test(expected = TypedException.class)
  public void invalidStringPayloadUsingParser() throws TypedException, BadRequestException, ExecutionException {
    String payload = "{ \"naazame\": \"Major League Soccer\" }";
    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);

    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestJsonSchemaValidator jsonSchemavalidator =
        new RestJsonSchemaValidator(config.getJsonSchema("/leagues,POST,application/json").getSchema());
    jsonSchemavalidator.validate(payload);
  }

}
