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
import org.mule.module.apikit.validation.body.schema.v1.RestXmlSchemaValidator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.exception.TypedException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class RestXMLSchemaValidatorTestCase {

  private static final String xmlSchema = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +
      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
      " elementFormDefault=\"qualified\" xmlns=\"http://mulesoft.com/schemas/soccer\"" +
      " targetNamespace=\"http://mulesoft.com/schemas/soccer\">" +
      "<xs:element name=\"league\">" +
      "  <xs:complexType>" +
      "    <xs:sequence>" +
      "      <xs:element name=\"name\" type=\"xs:string\"/>" +
      "      <xs:element name=\"description\" type=\"xs:string\" minOccurs=\"0\"/>" +
      "    </xs:sequence>" +
      "  </xs:complexType>" +
      "</xs:element>" +
      "</xs:schema>";
  private static IRaml api;

  @BeforeClass
  public static void mockApi() {
    api = Mockito.mock(IRaml.class);

    Map<String, Object> compiledSchemaMap = new HashMap<>();
    Schema compiledSchema = org.raml.parser.visitor.SchemaCompiler.getInstance().compile(xmlSchema);
    compiledSchemaMap.put("scheme-xml", compiledSchema);
    when(api.getCompiledSchemas()).thenReturn(compiledSchemaMap);

    Map<String, String> schemaMap = new HashMap<>();
    schemaMap.put("scheme-xml", xmlSchema);
    when(api.getConsolidatedSchemas()).thenReturn(schemaMap);

    Map<String, IMimeType> body = new HashMap<>();
    IMimeType mimeType = Mockito.mock(IMimeType.class);
    when(mimeType.getType()).thenReturn("application/xml");
    when(mimeType.getSchema()).thenReturn("scheme-xml");
    body.put("application/xml", mimeType);
    IAction mockedAction = Mockito.mock(IAction.class);
    when(mockedAction.getBody()).thenReturn(body);
    IResource mockedResource = Mockito.mock(IResource.class);
    when(mockedResource.getAction("POST")).thenReturn(mockedAction);
    when(api.getResource("/leagues")).thenReturn(mockedResource);
  }

  @Test
  public void validStringPayloadUsingParser() throws TypedException, ExecutionException, BadRequestException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>";
    String schemaPath = "/leagues,POST,application/xml";

    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestXmlSchemaValidator xmlValidator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath));

    xmlValidator.validate(payload);
  }

  @Test(expected = TypedException.class)
  public void invalidStringPayloadUsingParser() throws TypedException, BadRequestException, ExecutionException {
    String payload = "<league xmlns=\"http://mulesoft.com/schemas/soccer\"><invalid>hello</invalid></league>";
    String schemaPath = "/leagues,POST,application/xml";

    Configuration config = new Configuration();
    RamlHandler ramlHandler = Mockito.mock(RamlHandler.class);
    when(ramlHandler.getApi()).thenReturn(api);
    config.setRamlHandler(ramlHandler);

    RestXmlSchemaValidator xmlValidator = new RestXmlSchemaValidator(config.getXmlSchema(schemaPath));
    xmlValidator.validate(payload);
  }
}
