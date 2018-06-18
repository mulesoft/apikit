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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.interfaces.model.ApiVendor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_10;

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
  public void apiVendor() {
    final ApiVendor expected = isRaml08 ? RAML_08 : RAML_10;
    assertThat(amfWrapper.getApiVendor(), is(expected));
    assertThat(ramlWrapper.getApiVendor(), is(expected));
  }

  @Test
  public void dump() {
    final String amfDump = amfWrapper.dump(amf, "http://apikit-test");
    final String ramlDump = ramlWrapper.dump(raml, "http://apikit-test");
    // assertThat(amfDump, is(equalTo(ramlDump)));

    //assertThat(amfDump, containsString("title"));
    //assertThat(ramlDump, containsString("title"));
  }

  @Test
  public void raml() {
    assertEqual(amf, raml);
  }

}
