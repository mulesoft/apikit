/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static org.mockito.Mockito.when;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.v1.RestJsonSchemaValidator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.TypedException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

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


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
    when(mockedResource.getResolvedUri(api.getVersion())).thenReturn("/leagues");

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

  @Test(expected = BadRequestException.class)
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

  @Test
  public void showAllSchemaValidationErrors() throws TypedException, BadRequestException, ExecutionException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(VALIDATION_ERRORS_EXPECTED_MESSAGE);

    String payload = "{ \"id\": 1 }";
    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);

    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestJsonSchemaValidator jsonSchemavalidator =
        new RestJsonSchemaValidator(config.getJsonSchema("/leagues,POST,application/json").getSchema());
    jsonSchemavalidator.validate(payload);
  }

  private static final String VALIDATION_ERRORS_EXPECTED_MESSAGE =
      "error: object has missing required properties ([\"name\"])\n" +
          "    level: \"error\"\n" +
          "    schema: {\"loadingURI\":\"#\",\"pointer\":\"\"}\n" +
          "    instance: {\"pointer\":\"\"}\n" +
          "    domain: \"validation\"\n" +
          "    keyword: \"properties\"\n" +
          "    required: [\"name\"]\n" +
          "    missing: [\"name\"]\n" +
          "\n" +
          "error: instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])\n" +
          "    level: \"error\"\n" +
          "    schema: {\"loadingURI\":\"#\",\"pointer\":\"/properties/id\"}\n" +
          "    instance: {\"pointer\":\"/id\"}\n" +
          "    domain: \"validation\"\n" +
          "    keyword: \"type\"\n" +
          "    found: \"integer\"\n" +
          "    expected: [\"string\"]\n" +
          "\n";
}
