/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.stubbing.Stubber;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.RuntimeEdition;

import static java.util.Collections.EMPTY_MAP;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public abstract class AbstractScaffolderTestCase extends AbstractMultiParserTestCase {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();
  protected Log logger;


  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }

  protected File createTmpFile(final String resource) throws IOException {
    final String[] parts = resource.split("/");

    final String[] folderNames = Arrays.copyOf(parts, parts.length - 1);

    final File tmpFolder = folder.newFolder(folderNames);

    return createTmpFile(tmpFolder, resource);
  }

  protected File createTmpFile(final File tmpFolder, final String resource) throws IOException {
    final Path path = Paths.get(resource);
    final String fileName = path.getFileName().toString();

    final File tmpFile = new File(tmpFolder, fileName);
    tmpFile.createNewFile();
    InputStream resourceAsStream = ScaffolderMule4Test.class.getClassLoader().getResourceAsStream(resource);
    IOUtils.copy(resourceAsStream, new FileOutputStream(tmpFile));
    return tmpFile;
  }


  protected final Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
    if (ramls == null) {
      return EMPTY_MAP;
    }
    return fileListUtils.toFiles(ramls, element -> element);
  }

  protected File createTmpMuleXmlOutFolder() throws IOException {
    return createTmpMuleXmlOutFolder(folder.newFolder("mule-xml-out"));
  }

  protected File createTmpMuleXmlOutFolder(final File folder) throws IOException {
    createTmpFile(folder, "mule-artifact.json");
    return folder;
  }

  protected static String fileNameWhithOutExtension(final String path) {
    return FilenameUtils.removeExtension(Paths.get(path).getFileName().toString());
  }

  protected Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, null, null);
  }

  protected Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, domainFile, null);
  }

  protected Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                        Set<File> ramlsWithExtensionEnabled)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, domainFile, ramlsWithExtensionEnabled, DEFAULT_MULE_VERSION, EE);
  }

  protected Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                        Set<File> ramlsWithExtensionEnabled, String muleVersion, RuntimeEdition runtimeEdition)
      throws FileNotFoundException {

    Map<File, InputStream> ramlMap = null;
    if (ramls != null) {
      ramlMap = getFileInputStreamMap(ramls);
    }
    Map<File, InputStream> xmlMap = getFileInputStreamMap(xmls);
    InputStream domainStream = null;
    if (domainFile != null) {
      domainStream = new FileInputStream(domainFile);
    }
    return new Scaffolder(getLogger(), muleXmlOut, ramlMap, xmlMap, domainStream, ramlsWithExtensionEnabled, muleVersion,
                          runtimeEdition);
  }

  private Log getLogger() {
    if (logger == null) {
      logger = mock(Log.class);
      getStubber("[INFO] ").when(logger).info(anyString());
      getStubber("[WARNING] ").when(logger).warn(anyString());
      getStubber("[ERROR] ").when(logger).error(anyString());
    }

    return logger;
  }

  private static Stubber getStubber(final String prefix) {
    return doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      System.out.println(prefix + args[0].toString());
      return null;
    });
  }
}
