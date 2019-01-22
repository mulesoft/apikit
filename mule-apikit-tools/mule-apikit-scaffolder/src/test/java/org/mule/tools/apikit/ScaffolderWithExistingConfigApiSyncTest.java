/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;

public class ScaffolderWithExistingConfigApiSyncTest extends AbstractScaffolderTestCase {

  private final static ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("rescaffolding-apisync-version");
    folder.newFolder("rescaffolding-apisync-version", "v1");
    folder.newFolder("rescaffolding-apisync-version", "v2");
  }

  @Test
  public void reScaffold() throws Exception {
    final List<File> ramls = singletonList(getFile("rescaffolding-apisync-version/v1/api.raml"));
    File outputFolder = folder.newFolder("mule-xml-out");

    final File muleXmlOut = new File(outputFolder, "api.xml");

    assertFalse(muleXmlOut.exists());
    Scaffolder scaffolder = createScaffolder(ramls, null, outputFolder, null, false);
    scaffolder.run();

    assertTrue(muleXmlOut.exists());
    final URI expected = getClass().getResource("/rescaffolding-apisync-version/v1/api.xml").toURI();
    assertEquals("First Scaffolding differs from expected", IOUtils.toString(expected), IOUtils.toString(muleXmlOut.toURI()));

    List<File> xmls = singletonList(muleXmlOut);
    scaffolder = createScaffolder(ramls, xmls, outputFolder, null, false);
    scaffolder.run();

    assertTrue(muleXmlOut.exists());
    assertEquals("Second Scaffolding differs from expected", IOUtils.toString(expected), IOUtils.toString(muleXmlOut.toURI()));
  }

  @Test
  public void reScaffoldDifferntVersions() throws Exception {
    final List<File> ramlsV1 = singletonList(getFile("rescaffolding-apisync-version/v1/api.raml"));
    final File outputFolder = folder.newFolder("mule-xml-out");

    final File muleXmlOut = new File(outputFolder, "api.xml");
    assertFalse(muleXmlOut.exists());

    Scaffolder scaffolder = createScaffolder(ramlsV1, null, outputFolder, null, false);
    scaffolder.run();

    assertTrue(muleXmlOut.exists());
    final URI expectedV1 = getClass().getResource("/rescaffolding-apisync-version/v1/api.xml").toURI();
    assertEquals("First Scaffolding differs from expected", IOUtils.toString(expectedV1), IOUtils.toString(muleXmlOut.toURI()));

    List<File> xmls = singletonList(muleXmlOut);
    final List<File> ramlsV2 = singletonList(getFile("rescaffolding-apisync-version/v2/api.raml"));
    scaffolder = createScaffolder(ramlsV2, xmls, outputFolder, null, false);
    scaffolder.run();

    assertTrue(muleXmlOut.exists());
    final URI expectedV2 = getClass().getResource("/rescaffolding-apisync-version/v2/api.xml").toURI();
    assertEquals("Second Scaffolding differs from expected", IOUtils.toString(expectedV2), IOUtils.toString(muleXmlOut.toURI()));

  }

  private File getFile(String s) throws Exception {
    if (s == null) {
      return null;
    }
    final File file = folder.newFile(s);
    final InputStream resourceAsStream = ScaffolderWithExistingConfigApiSyncTest.class.getClassLoader().getResourceAsStream(s);
    assertNotNull(resourceAsStream);
    IOUtils.copy(resourceAsStream, new FileOutputStream(file));
    return file;
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      boolean compatibilityMode)
      throws IOException {
    Log log = mock(Log.class);
    Map<String, InputStream> ramlMap = null;
    if (ramls != null) {
      ramlMap = getRamlInputStreamMap(ramls);
    }
    Map<File, InputStream> xmlMap = getFileInputStreamMap(xmls);
    InputStream domainStream = null;
    if (domainFile != null) {
      domainStream = new FileInputStream(domainFile);
    }
    return new Scaffolder(log, muleXmlOut, ramlMap, scaffolderResourceLoaderMock, xmlMap, domainStream, DEFAULT_MULE_VERSION,
                          DEFAULT_RUNTIME_EDITION);
  }


  private Map<String, InputStream> getRamlInputStreamMap(List<File> ramls) throws IOException {
    Map<String, InputStream> map = new HashMap<>();

    for (File rootRaml : ramls) {
      String resourceFormat = "resource::com.mycompany:raml-api:%s:raml:zip:%s";
      String version = (rootRaml.getAbsolutePath().contains("v2") ? "2.0.0" : "1.0.0");
      String resource = String.format(resourceFormat, version, rootRaml.getName());

      map.put(resource, FileUtils.openInputStream(rootRaml));
      Mockito.doReturn(FileUtils.openInputStream(rootRaml)).doReturn(FileUtils.openInputStream(rootRaml))
          .when(scaffolderResourceLoaderMock)
          .getResourceAsStream(resource);
      Mockito.doReturn((rootRaml.toURI())).when(scaffolderResourceLoaderMock)
          .getResource(resource);
    }

    return map;
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}
