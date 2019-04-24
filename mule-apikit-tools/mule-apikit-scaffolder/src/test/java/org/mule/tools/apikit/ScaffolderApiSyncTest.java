/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mule.tools.apikit.model.ScaffolderReport;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;
import static org.mule.tools.apikit.model.Status.*;

public class ScaffolderApiSyncTest extends AbstractScaffolderTestCase {

  private final static String MULE_4_VERSION = "4.0.0";
  private final static ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);

  private final Dependency dependency = createDependency("com.mycompany", "raml-api", "1.0.0", "raml", "zip");
  private final static String ROOT_RAML_RESOURCE_URL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:";
  private final static String DEPENDENCIES_RESOURCE_URL = "resource::com.mycompany:raml-library:1.1.0:raml-fragment:zip:";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testSimpleGenerationV08() throws Exception {
    final String ramlFolder = "src/test/resources/scaffolder/";
    final String rootRaml = "simpleV10";

    testSimple(ramlFolder, rootRaml);
  }


  @Test
  public void testSimpleGenerationV10() throws Exception {
    final String rootRaml = "simpleV10";
    final String ramlFolder = "src/test/resources/scaffolder/";

    testSimple(ramlFolder, rootRaml);
  }

  @Test
  public void testRAMLWithoutResources() throws Exception {
    File api = generateApi("src/test/resources/api-sync/empty-api", "without-resources",
                           SUCCESS.toString());

    assertTrue(api.exists());
    assertEquals("Files are different", FileUtils
        .readFileToString(new File(
                                   getClass().getClassLoader().getResource("api-sync/empty-api/expected-result.xml")
                                       .getFile()))
        .replaceAll("\\s+", ""),
                 FileUtils.readFileToString(api).replaceAll("\\s+", ""));
  }

  @Test
  public void testRAMLWithCharset() throws Exception {
    File api = generateApi("src/test/resources/api-sync/api-raml-with-charset",
                           "api", SUCCESS.toString());

    assertTrue(api.exists());
    assertEquals("Files are different", FileUtils
        .readFileToString(new File(getClass().getClassLoader()
            .getResource("api-sync/api-raml-with-charset/expected-result.xml").getFile()))
        .replaceAll("\\s+", ""), FileUtils.readFileToString(api).replaceAll("\\s+", ""));
  }

  @Test
  public void generateWithIncludes10() throws Exception {

    final String rootRaml = "api";
    final String ramlFolder = "src/test/resources/api-sync/scaffolder-include-10/";

    File xmlOut = generateScaffolder(ramlFolder, rootRaml, Collections.singletonList("included.raml"));

    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertNotNull(s);
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "post:\\Queue:application\\json:" + rootRaml + "-config"));
    assertEquals(2, countOccurences(s, "post:\\Queue:text\\xml:" + rootRaml + "-config"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void apiWithErrors() throws Exception {

    final String rootRaml = "simpleV10-with-errors";
    final String ramlFolder = "src/test/resources/api-sync/with-errors/";

    generateScaffolder(ramlFolder, rootRaml, null, ramlFolder, null, FAILED.toString());
  }


  @Test
  public void libraryReferenceToRoot() throws Exception {
    final String rootRaml = "test-api";
    final String ramlFolder = "src/test/resources/api-sync/library-reference-to-root/root/";
    final String libraryFolder = "src/test/resources/api-sync/library-reference-to-root/library/";
    final List<String> libraryFiles = Arrays.asList("library.raml", "reused-fragment.raml");

    File xmlOut =
        generateScaffolder(ramlFolder, rootRaml, libraryFiles, libraryFolder, Collections.singletonList("library.raml"), null);

    assertTrue(xmlOut.exists());

  }

  private void testSimple(String ramlFolder, String rootRaml) throws Exception {
    List<Dependency> dependencyList = new ArrayList<>();
    dependencyList.add(dependency);
    File muleXmlSimple = generateScaffolder(ramlFolder, rootRaml);

    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

    assertSimple(s, rootRaml);
  }

  private File generateScaffolder(String ramlFolder, String rootRaml)
      throws Exception {
    return generateScaffolder(ramlFolder, rootRaml, null);
  }

  private File generateScaffolder(String ramlFolder, String rootRaml, List<String> referencedFiles) throws Exception {
    return generateScaffolder(ramlFolder, rootRaml, referencedFiles, ramlFolder, null, null);
  }

  private File generateScaffolder(String ramlFolder, String rootRaml, List<String> referencedFiles, String referencedFilesFolder,
                                  List<String> rootRamlFiles, String expectedStatus)
      throws Exception {
    final String exchangeJsonResourceURL = ROOT_RAML_RESOURCE_URL + "exchange.json";
    final String rootRamlResourceURL = ROOT_RAML_RESOURCE_URL + rootRaml + ".raml";

    if (expectedStatus == null)
      expectedStatus = SUCCESS.toString();

    mockScaffolderResourceLoader(exchangeJsonResourceURL, ramlFolder, rootRaml + ".json");
    mockScaffolderResourceLoader(rootRamlResourceURL, ramlFolder, rootRaml + ".raml");

    if (rootRamlFiles != null) {
      for (String rootRamlFile : rootRamlFiles) {
        mockScaffolderResourceLoader(DEPENDENCIES_RESOURCE_URL + rootRamlFile, ramlFolder, rootRamlFile);
      }
    }

    if (referencedFiles != null) {
      for (String file : referencedFiles) {
        mockScaffolderResourceLoader(DEPENDENCIES_RESOURCE_URL + file, referencedFilesFolder, file);
      }
    }

    File muleXmlOut = folder.newFolder("mule-xml-out");

    ScaffolderReport scaffolderReport = new ScaffolderAPI().run(Collections.singletonList(dependency),
                                                                scaffolderResourceLoaderMock, muleXmlOut, null, MULE_4_VERSION,
                                                                EE);

    assertEquals(expectedStatus, scaffolderReport.getStatus());
    return new File(muleXmlOut, rootRaml + ".xml");
  }

  private void mockScaffolderResourceLoader(String resourceURL, String folder, String file) throws Exception {
    Mockito.doReturn(getToBeReturned(folder, file)).when(scaffolderResourceLoaderMock)
        .getResource(resourceURL);
    Mockito.doReturn(getInputStream(folder + file)).doReturn(getInputStream(folder + file))
        .doReturn(getInputStream(folder + file)).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(resourceURL);
  }

  private URI getToBeReturned(String folder, String file) {
    return new File(folder + file).toURI();
  }

  private void assertSimple(String s, String listenerConfigName) {
    assertEquals(1, countOccurences(s, "http:listener-config name=\"" + listenerConfigName));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "#[vars.outboundHeaders default {}]"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(2, countOccurences(s, "<ee:set-variable variableName=\"name\">attributes.uriParams.name</ee:set-variable>"));
    assertEquals(1, countOccurences(s, "<ee:set-variable variableName=\"owner\">attributes.uriParams.owner</ee:set-variable>"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(8, countOccurences(s, "http:headers"));
    assertEquals(2, countOccurences(s, "get:\\:" + listenerConfigName + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + listenerConfigName + "-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[null]"));
    assertEquals(0,
                 countOccurences(s,
                                 "expression-component>mel:flowVars['variables.outboundHeaders default {}'].put('Content-Type', 'application/json')</expression-component>"));
    assertEquals(0,
                 countOccurences(s,
                                 "set-variable variableName=\"variables.outboundHeaders default {}\" value=\"#[mel:new java.util.HashMap()]\" />"));
    assertEquals(0, countOccurences(s, "exception-strategy"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private InputStream getInputStream(String resourcePath) throws FileNotFoundException {
    return new FileInputStream(resourcePath);
  }

  private static Dependency createDependency(String groupId, String artifactId, String version, String classifier, String type) {
    Dependency dependency = new Dependency();

    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setClassifier(classifier);
    dependency.setType(type);

    return dependency;
  }


  @Test
  public void testRaml08Fallback() throws Exception {
    if (isAmf()) {
      generateApi("src/test/resources/api-sync/fallback-raml-08", "api", FAILED.toString());
    } else {
      assertTrue(generateApi("src/test/resources/api-sync/fallback-raml-08", "api", SUCCESS.toString()).exists());
    }
  }

  private File generateApi(String ramlFolder, String rootRaml, String expectedStatus)
      throws Exception {

    if (expectedStatus == null)
      expectedStatus = SUCCESS_WITH_ERRORS.toString();

    File muleXmlOut = folder.newFolder("mule-xml-out");

    ScaffolderReport scaffolderReport = new ScaffolderAPI().run(Collections.singletonList(dependency),
                                                                new TestScaffolderResourceLoader(ramlFolder), muleXmlOut, null,
                                                                MULE_4_VERSION,
                                                                EE);

    assertEquals(expectedStatus, scaffolderReport.getStatus());
    return new File(muleXmlOut, rootRaml + ".xml");
  }
}
