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
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;

public class ScaffolderWithExistingConfigApiSyncTest {

  private final static ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("rescaffolding-apisync-version");
    folder.newFolder("rescaffolding-apisync-version", "v1");
    folder.newFolder("rescaffolding-apisync-version", "v2");
  }

  @Test
  public void reScaffold() throws Exception {
    List<File> ramls = Arrays.asList(getFile("rescaffolding-apisync-version/api.raml"));
    File muleXmlOut = folder.newFolder("mule-xml-out");

    assertFalse((new File(muleXmlOut.getAbsolutePath() + "/api.xml")).exists());
    Scaffolder scaffolder = createScaffolder(ramls, null, muleXmlOut, null, false, null);
    scaffolder.run();

    File api = new File(muleXmlOut.getAbsolutePath() + "/api.xml");
    assertTrue(api.exists());

    File apiBkp = File.createTempFile("apikp", ".xml");
    FileUtils.copyFile(api, apiBkp);

    List<File> xmls = Arrays.asList(api);

    scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, false, null);
    scaffolder.run();

    api = new File(muleXmlOut.getAbsolutePath() + "/api.xml");
    assertTrue(api.exists());

    assertEquals(IOUtils.toString(new FileInputStream(api)), IOUtils.toString(new FileInputStream(apiBkp)));

  }

  @Test
  public void reScaffoldDifferntVersions() throws Exception {
    List<File> ramls = Arrays.asList(getFile("rescaffolding-apisync-version/v1/api.raml"));
    File muleXmlOut = folder.newFolder("mule-xml-out");

    assertFalse((new File(muleXmlOut.getAbsolutePath() + "/api.xml")).exists());
    Scaffolder scaffolder = createScaffolder(ramls, null, muleXmlOut, null, false, null);
    scaffolder.run();

    File api = new File(muleXmlOut.getAbsolutePath() + "/api.xml");
    assertTrue(api.exists());

    File apiBkp = File.createTempFile("apikp", ".xml");
    FileUtils.copyFile(api, apiBkp);

    List<File> xmls = Arrays.asList(api);
    ramls = Arrays.asList(getFile("rescaffolding-apisync-version/v2/api.raml"));
    scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, false, null);
    scaffolder.run();

    api = new File(muleXmlOut.getAbsolutePath() + "/api.xml");
    assertTrue(api.exists());

    assertEquals(IOUtils.toString(new FileInputStream(api)), IOUtils.toString(new FileInputStream(apiBkp)));

  }

  private File getFile(String s) throws Exception {
    if (s == null) {
      return null;
    }
    File file = folder.newFile(s);
    file.createNewFile();
    InputStream resourceAsStream = ScaffolderWithExistingConfigApiSyncTest.class.getClassLoader().getResourceAsStream(s);
    IOUtils.copy(resourceAsStream,
                 new FileOutputStream(file));
    return file;
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      boolean compatibilityMode, Set<File> ramlsWithExtensionEnabled)
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

  private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
    if (ramls == null) {
      return EMPTY_MAP;
    }
    return fileListUtils.toStreamFromFiles(ramls);
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
    }

    return map;
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}
