/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

abstract class AbstractCompatibilityTestCase extends AbstractTestCase {

  private final IRaml amf;
  private IRaml raml;

  AbstractCompatibilityTestCase(final String api) {
    amf = ParserAmfUtils.build(api);
    assertNotNull(amf);
    raml = buildRaml(api);
    assertNotNull(raml);
  }

  abstract IRaml buildRaml(final String api);

  @Test
  public void baseUri() {
    final String ramlBaseUri = raml.getBaseUri();
    final String amfBaseUri = amf.getBaseUri();

    assertThat(ramlBaseUri, is(equalTo(amfBaseUri)));
  }

  @Ignore
  public void uri() {
    final String ramlUri = raml.getBaseUri();
    final String amfUri = amf.getBaseUri();
    assertThat(ramlUri, is(equalTo(amfUri)));
  }

  @Test
  public void version() {
    final String ramlVersion = raml.getVersion();
    final String amfVersion = amf.getVersion();
    assertThat(ramlVersion, is(equalTo(amfVersion)));
  }

  @Test
  public void baseUriParameters() {
    final Map<String, IParameter> ramlBaseUriParameters = raml.getBaseUriParameters();
    final Map<String, IParameter> amfBaseUriParameters = amf.getBaseUriParameters();

    assertThat(ramlBaseUriParameters.size(), is(amfBaseUriParameters.size()));

    ramlBaseUriParameters.forEach((k, v) -> {
      assertThat(amfBaseUriParameters.containsKey(k), is(true));
      assertResourcesEqual(v, amfBaseUriParameters.get(k));
    });
  }

  @Test
  public void resources() {
    final Map<String, IResource> ramlResources = raml.getResources();
    final Map<String, IResource> amfResources = amf.getResources();

    assertResourcesEqual(amfResources, ramlResources);
  }

}
