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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
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
  public void dump() {//throws Exception {
    final String amfDump = amfWrapper.dump(amf, "http://apikit-test");
    final String ramlDump = ramlWrapper.dump(raml, "http://apikit-test");

    // Dump to file
    final Path basePath = Paths.get(input.getPath()).getParent();
    final Path amfDumpPath = basePath.resolve("amf-dump.raml");
    final Path ramlDumpPath = basePath.resolve("raml-dump.raml");

    try {
      Files.write(amfDumpPath, amfDump.getBytes("UTF-8"));
    } catch (IOException e) {
      Assert.fail("Error persisting AMF dump file");
      e.printStackTrace();
    }

    try {
      Files.write(ramlDumpPath, ramlDump.getBytes("UTF-8"));
    } catch (IOException e) {
      Assert.fail("Error persisting RAML dump file");
      e.printStackTrace();
    }

    // Parse java dumped file  
    final ParserWrapper dumpedRamlWrapper = createJavaParserWrapper(ramlDumpPath.toUri().toString(), isRaml08);
    final IRaml dumpedRaml = dumpedRamlWrapper.build();
    assertNotNull(dumpedRaml);

    // TODO APIKIT-1380
    // Parse amf dumpled file
    if (!basePath.toString().endsWith("08-leagues")) {
      try {
        final ParserWrapper dumpedAmfWrapper = ParserWrapperAmf.create(amfDumpPath.toUri(), true);
        final IRaml dumpedAmf = dumpedAmfWrapper.build();
        assertNotNull(dumpedAmf);
        assertEqual(dumpedAmf, dumpedRaml);
      } catch (Exception e) {
        Assert.fail("Error parsing AMF dumped file:\n" + e.getMessage());
      }
    }
  }

  @Test
  public void raml() {
    assertEqual(amf, raml);
  }

}
