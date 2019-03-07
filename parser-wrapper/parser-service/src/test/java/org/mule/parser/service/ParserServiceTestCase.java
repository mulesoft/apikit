/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.api.ApiRef;

import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.raml.interfaces.model.ApiVendor.OAS_20;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

public class ParserServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void raml08Wrapper() throws URISyntaxException {

    final String api = resource("/api-08.raml");

    ParserService parserService = new ParserService();
    final ParserWrapper wrapper = parserService.getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
    assertThat(parserService.getParsingErrors().size(), is(0));
  }

  @Test
  public void raml10Wrapper() throws URISyntaxException {

    final String api = resource("/api-10.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void raml10AmfWrapper() throws URISyntaxException {

    final String api = resource("/example-with-include/example-with-include.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(RAML_10));
  }

  @Test
  public void oasJson20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.json");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void oasYaml20Wrapper() throws URISyntaxException {

    final String api = resource("/petstore.yaml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.AMF);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.AMF));
    assertThat(wrapper.getApiVendor(), is(OAS_20));
  }

  @Test
  public void fallbackParser() {
    ParserService parserService = new ParserService();
    ParserWrapper wrapper = parserService.getParser(ApiRef.create(resource("/api-with-fallback-parser.raml")));

    assertNotNull(wrapper);
    assertThat(parserService.getParsingErrors().size(), is(1));
    assertThat(parserService.getParsingErrors().get(0).cause().contains("Validation failed using parser type : AMF, in file :"),
               is(true));
  }

  @Test
  public void invalidRAML() {
    expectedException.expect(ParserServiceException.class);
    ParserService parserService = new ParserService();
    try {
      parserService.getParser(ApiRef.create(resource("/with-invalid-errors.raml")));
    } finally {
      assertThat(parserService.getParsingErrors().size(), is(2));
      assertThat(parserService.getParsingErrors().get(0).cause().contains("Validation failed using parser type : AMF, in file :"),
                 is(true));
      assertThat(parserService.getParsingErrors().get(1).cause()
          .contains("Validation failed using fallback parser type : RAML, in file :"),
                 is(true));

    }
  }

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
