/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;

@RunWith(Parameterized.class)
public class MuleArtifactJsonGeneratorTestCase {

  private String testName;
  private String expected;

  private static final String BASE_DIR = "src/test/resources/org/mule/tools/apikit/output/mule-artifact";

  public MuleArtifactJsonGeneratorTestCase(String testName, String expected) {
    this.testName = testName;
    this.expected = expected;
  }

  @Parameterized.Parameters
  public static Collection testCases() {
    return Arrays.asList(new Object[][] {
        {"new-artifact-json", "expected.json"},
        {"existent-artifact-json", "expected.json"},
        {"empty-artifact-json", "expected.json"}});
  }

  @Test
  public void testGenerateNewDescriptor() throws Exception {
    final Log log = Mockito.mock(Log.class);
    final MuleArtifactJsonGenerator configGenerator =
        new MuleArtifactJsonGenerator(log, new File(BASE_DIR, testName), DEFAULT_MULE_VERSION);

    final String json = configGenerator.generateArtifact();

    final String expectedJson = IOUtils.toString(new FileInputStream(new File(BASE_DIR, testName + "/" + expected)));

    assertEquals(expectedJson, json);
  }

}
