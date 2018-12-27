/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import org.junit.Test;
import org.mule.raml.interfaces.ParserType;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.api.ApiRef;

import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mule.raml.interfaces.model.ApiVendor.*;

public class ParserServiceTestCase {

  @Test
  public void raml08Wrapper() throws URISyntaxException {

    final String api = resource("/api-08.raml");

    final ParserWrapper wrapper = new ParserService().getParser(ApiRef.create(api), ParserType.RAML);
    assertNotNull(wrapper);
    assertThat(wrapper.getParserType(), is(ParserType.RAML));
    assertThat(wrapper.getApiVendor(), is(RAML_08));
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

  private static String resource(final String path) {
    return ResourcesUtils.resource(ParserServiceTestCase.class, path);
  }
}
