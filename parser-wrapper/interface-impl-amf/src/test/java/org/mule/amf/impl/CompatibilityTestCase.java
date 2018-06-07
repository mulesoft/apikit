/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CompatibilityTestCase extends AbstractCompatibilityTestCase {


  public CompatibilityTestCase(final File input, final String name) {
    super(input, name);
  }

  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {
    final URI baseFolder = CompatibilityTestCase.class.getResource("").toURI(); // 08-resources
    return getData(baseFolder);

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
  public void baseUri() {
    final String ramlBaseUri = raml.getBaseUri();
    final String amfBaseUri = amf.getBaseUri();

    assertThat(ramlBaseUri, is(equalTo(amfBaseUri)));

  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void cleanBaseUriParameters() {
    amf.cleanBaseUriParameters();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void compiledSchemas() {
    amf.getCompiledSchemas();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void consolidatedSchemas() {
    amf.getConsolidatedSchemas();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void instance() {
    amf.getInstance();
  }

  @Ignore("TODO")
  public void schemas() {
    amf.getSchemas();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void securitySchemes() {
    amf.getSecuritySchemes();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void traits() {
    amf.getTraits();
  }

  @Ignore("Different behaviour in Java Parser 08 & 10")
  public void uri() {
    final String ramlUri = raml.getUri();
    final String amfUri = amf.getUri();
    assertThat(ramlUri, is(equalTo(amfUri)));
  }

  @Test
  public void version() {
    final String ramlVersion = raml.getVersion();
    final String amfVersion = amf.getVersion();
    assertThat(ramlVersion, is(equalTo(amfVersion)));
  }

  @Test
  public void resources() {

    final Map<String, IResource> ramlResources = raml.getResources();
    final Map<String, IResource> amfResources = amf.getResources();

    //dump("Resources 08",  ramlResources);
    //dump("Resources AMF",  amfResources);

    assertResourcesEqual(amfResources, ramlResources);
  }

  private static void dump(final String title, Map<String, IResource> resources) {
    System.out.println(format("------------- %s -------------", title));
    System.out.println(dump("", resources, ""));
    System.out.println("-------------------------------------");
  }

  private static String dump(final String indent, Map<String, IResource> resources, String out) {

    if (resources.isEmpty())
      return out;

    for (Map.Entry<String, IResource> entry : resources.entrySet()) {

      final IResource value = entry.getValue();
      final Set<String> actions = value.getActions().keySet().stream().map(Enum::name).collect(toSet());
      final String resource = "[" + entry.getKey() + "] -> " + value.getUri() + " " + mkString(actions);
      out += indent + resource + "\n";
      if (value.getResources().isEmpty())
        continue;

      out = dump(indent + "  ", value.getResources(), out);
    }
    return out;
  }
}
