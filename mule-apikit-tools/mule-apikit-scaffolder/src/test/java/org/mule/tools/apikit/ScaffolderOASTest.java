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
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.stubbing.Stubber;
import org.mule.amf.impl.DocumentParser;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.RuntimeEdition;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mule.amf.impl.DocumentParser.VendorEx.OAS20_JSON;
import static org.mule.amf.impl.DocumentParser.VendorEx.OAS20_YAML;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

@RunWith(Parameterized.class)
public class ScaffolderOASTest {

  private String folderName;
  private Path api;

  private Log logger;
  private FileListUtils fileListUtils = new FileListUtils();

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.{json,yaml, yml}");

  public ScaffolderOASTest(final String folderName, final Path api) {

    this.folderName = folderName;
    this.api = api;
  }

  @Before
  public void beforeTest() throws IOException {
    final File outputFolder = outputFolder(api).toFile();

    if (outputFolder.exists())
      FileUtils.deleteDirectory(outputFolder);
  }

  @Test
  public void scaffolder() throws Exception {

    final Path muleApp = simpleGeneration(api);

    final String current = readFile(muleApp);
    if (current.trim().isEmpty()) {
      Assert.fail(format("Scaffolder generation fail parsing API '%s'", api.getFileName()));
    }

    final Path goldenPath = goldenFile();

    // When Golden file is missing we create it but test fail
    if (!goldenPath.toFile().exists()) {
      createGoldenFile(goldenPath, current);
      Assert.fail(format("Golden file missing. Created for API '%s'", api.getFileName()));
    }

    // When Golden file existe we comparate both Scaffolder versions
    final String expected = readFile(goldenPath);
    assertThat(format("Scaffolder differs for API '%s'", api.getFileName()), current,
               is(equalTo(expected)));
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {

    final List<Object[]> parameters = new ArrayList<>();
    final Path basePath = Paths.get(ScaffolderOASTest.class.getResource("/oas").toURI());

    scan(basePath).forEach(path -> {
      try {
        final Path folderName = basePath.relativize(path);
        parameters.add(new Object[] {folderName.toString(), path});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return parameters;
  }

  private Path goldenFile() {
    return muleApp(api.getParent(), fileNameWithOutExtension(api));
  }

  private static Path muleApp(final Path folder, final String apiName) {
    return Paths.get(folder.toString(), apiName + ".xml");
  }

  private static List<Path> scan(final Path root) throws IOException {
    return Files.walk(root)
        //.peek(path -> System.out.println("Path:" + path + " isApi:" + isOas(path)))
        .filter(ScaffolderOASTest::isOas)
        .collect(toList());
  }

  private static boolean isOas(final Path path) {
    final Path fileName = path.getFileName();
    final boolean isOas =
        Files.isRegularFile(path) && API_MATCHER.matches(fileName) && !"mule-artifact.json".equals(fileName.toString());

    if (!isOas)
      return false;

    final DocumentParser.VendorEx vendor = DocumentParser.getVendor(path.toUri());
    return OAS20_JSON.equals(vendor) || OAS20_YAML.equals(vendor);
  }

  private Path simpleGeneration(final Path api) throws Exception {

    final Path outputFolder = createOutputFolder(api);

    createScaffolder(singletonList(api.toFile()), emptyList(), outputFolder.toFile(), null, null, DEFAULT_MULE_VERSION, EE).run();

    return muleApp(outputFolder, fileNameWithOutExtension(api));
  }

  private Path createOutputFolder(final Path api) throws IOException {

    final Path outputFolder = outputFolder(api);
    Files.createDirectory(outputFolder);

    // Initialize mule ap & mule-artifact
    final String muleApp = fileNameWithOutExtension(api) + ".xml";
    Files.createFile(Paths.get(outputFolder.toString(), muleApp));
    final Path artifact = Files.createFile(Paths.get(outputFolder.toString(), "mule-artifact.json"));

    //artifact.toFile().createNewFile(); 
    InputStream resourceAsStream = ScaffolderOASTest.class.getClassLoader().getResourceAsStream("mule-artifact.json");
    IOUtils.copy(resourceAsStream, new FileOutputStream(artifact.toFile()));

    return outputFolder;
  }

  private static String fileNameWithOutExtension(final Path path) {
    return FilenameUtils.removeExtension(path.getFileName().toString());
  }

  private static Path outputFolder(final Path api) throws IOException {

    final String apiName = fileNameWithOutExtension(api);
    final Path apiFolder = api.getParent();
    return Paths.get(apiFolder.toString(), apiName);
  }

  private static Path createGoldenFile(final Path goldenFile, final String content) throws IOException {

    final String srcPath = goldenFile.toFile().getPath().replace("target/test-classes", "src/test/resources");
    final Path goldenPath = Paths.get(srcPath);
    System.out.println("*** Create Golden " + goldenPath);

    // Write golden files  with current values
    final Path parent = goldenPath.getParent();
    if (!Files.exists(parent))
      Files.createDirectory(parent);
    return Files.write(goldenPath, content.getBytes("UTF-8"));
  }

  private static String readFile(final Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /************************************************************************************************
  // Similar to AbstractScaffolderTestCase but single Parser
   ************************************************************************************************/

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      Set<File> ramlsWithExtensionEnabled, String muleVersion, RuntimeEdition runtimeEdition)
      throws FileNotFoundException {

    Map<File, InputStream> ramlMap = null;
    if (ramls != null) {
      ramlMap = toStreamMap(ramls);
    }
    Map<File, InputStream> xmlMap = toStreamMap(xmls);
    InputStream domainStream = null;
    if (domainFile != null) {
      domainStream = new FileInputStream(domainFile);
    }
    return new Scaffolder(getLogger(), muleXmlOut, ramlMap, xmlMap, domainStream, ramlsWithExtensionEnabled, muleVersion,
                          runtimeEdition);
  }

  private Map<File, InputStream> toStreamMap(List<File> ramls) {
    return fileListUtils.toStreamFromFiles(ramls);
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
