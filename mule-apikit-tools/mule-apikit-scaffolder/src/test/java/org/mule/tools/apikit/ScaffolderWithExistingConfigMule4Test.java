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
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;

public class ScaffolderWithExistingConfigMule4Test {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Before
  public void setUp() {
    folder.newFolder("scaffolder");
    folder.newFolder("scaffolder-existing");
    folder.newFolder("scaffolder-existing-multiples");
    folder.newFolder("scaffolder-existing-extension");
    folder.newFolder("scaffolder-existing-custom-lc");
    folder.newFolder("scaffolder-existing-old");
    folder.newFolder("scaffolder-existing-old-address");
    folder.newFolder("scaffolder-existing-custom-and-normal-lc");
    folder.newFolder("custom-domain-4");
    folder.newFolder("empty-domain");
    folder.newFolder("custom-domain-multiple-lc-4");
  }

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstFlowsXml() throws Exception {
    File resourcesFlows = getFile("scaffolder-existing-multiples/resources-flows.xml");
    File noResourcesFlows = getFile("scaffolder-existing-multiples/no-resources-flows.xml");
    List<File> ramls = singletonList(getFile("scaffolder-existing-multiples/api.raml"));

    List<File> xmls = Arrays.asList(resourcesFlows, noResourcesFlows);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    createScaffolder(ramls, xmls, muleXmlOut, null, false, null).run();

    assertTrue(resourcesFlows.exists());
    assertTrue(noResourcesFlows.exists());
    String s = IOUtils.toString(new FileInputStream(noResourcesFlows));
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstNoFlowsXml() throws Exception {
    File noResourcesFlows = getFile("scaffolder-existing-multiples/no-resources-flows.xml");
    File resourcesFlows = getFile("scaffolder-existing-multiples/resources-flows.xml");
    List<File> ramls = singletonList(getFile("scaffolder-existing-multiples/api.raml"));

    List<File> xmls = Arrays.asList(noResourcesFlows, resourcesFlows);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    createScaffolder(ramls, xmls, muleXmlOut, null, false, null).run();

    assertTrue(noResourcesFlows.exists());
    assertTrue(resourcesFlows.exists());
    String s = IOUtils.toString(new FileInputStream(noResourcesFlows));
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  private void testAlreadyExistsMultipleConfigurations(String firstConfiguration, String secondConfiguration) throws Exception {
    File firstConfigurationFile = getFile(firstConfiguration);
    File secondConfigurationFile = getFile(secondConfiguration);
    List<File> ramls = singletonList(getFile("scaffolder-existing-multiples/api.raml"));

    List<File> xmls = Arrays.asList(firstConfigurationFile, secondConfigurationFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    createScaffolder(ramls, xmls, muleXmlOut, null, false, null).run();

    assertTrue(firstConfigurationFile.exists());
    assertTrue(secondConfigurationFile.exists());
    File noResourceFlowsFile = getFile("scaffolder-existing-multiples/no-resources-flows.xml");
    String s = IOUtils.toString(new FileInputStream(noResourceFlowsFile));
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  @Test
  public void testAlreadyExistsWithExtensionDisabledWithOldParser() throws Exception {
    testAlreadyExistsWithExtensionDisabled();
  }

  @Test
  public void testAlreadyExistsWithExtensionDisabledWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsWithExtensionDisabled();
  }

  public void testAlreadyExistsWithExtensionDisabled() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
    File xmlFile = getFile("scaffolder-existing-extension/simple-extension-disabled-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
    createScaffolder(ramls, xmls, muleXmlOut, null, false, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(1, countOccurences(s, "extensionEnabled=\"false\""));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsWithExtensionEnabledWithOldParser() throws Exception {
    testAlreadyExistsWithExtensionEnabled();
  }

  @Test
  public void testAlreadyExistsWithExtensionEnabledWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsWithExtensionEnabled();
  }

  public void testAlreadyExistsWithExtensionEnabled() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
    File xmlFile = getFile("scaffolder-existing-extension/simple-extension-enabled-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
    createScaffolder(ramls, xmls, muleXmlOut, null, false, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(1, countOccurences(s, "extensionEnabled=\"true\""));
    assertEquals(0, countOccurences(s, "#[mel:null]"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsWithExtensionNotPresentWithOldParser() throws Exception {
    testAlreadyExistsWithExtensionNotPresent();
  }

  @Test
  public void testAlreadyExistsWithExtensionNotPresentWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsWithExtensionNotPresent();
  }

  public void testAlreadyExistsWithExtensionNotPresent() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
    File xmlFile = getFile("scaffolder-existing-extension/simple-extension-not-present-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
    createScaffolder(ramls, xmls, muleXmlOut, null, false, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[mel:null]"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithOldParser() throws Exception {
    testAlreadyExistsGenerate();
  }

  @Test
  public void testAlreadyExistsGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerate();
  }

  public void testAlreadyExistsGenerate() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
    File xmlFile = getFile("scaffolder-existing/simple-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, false, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "#[payload]"));
    assertEquals(2, countOccurences(s, "http:body"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomDomainWithOldParser() throws Exception {
    testAlreadyExistsGenerateWithCustomDomain();
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerateWithCustomDomain();
  }

  public void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-lc/simple.raml"));
    File xmlFile = getFile("scaffolder-existing-custom-lc/simple-4.xml");
    File domainFile = getFile("custom-domain-4/mule-domain-config.xml");

    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");
    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, false, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener-connection"));

    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomAndNormalLCWithOldParser() throws Exception {
    testAlreadyExistsGenerateWithCustomAndNormalLC();
  }

  @Test
  public void testAlreadyExistsGenerateWithCustomAndNormalLCWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsGenerateWithCustomAndNormalLC();
  }

  public void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml"));
    File xmlFile = getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");
    File domainFile = getFile("custom-domain-4/mule-domain-config.xml");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, false, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(2, countOccurences(s, "get:\\leagues\\(leagueId)"));
    assertEquals(2, countOccurences(s, "post:\\leagues\\(leagueId)"));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "<http:listener config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistingMuleConfigWithApikitRouterWithOldParser() throws Exception {
    testAlreadyExistingMuleConfigWithApikitRouter();
  }

  @Test
  public void testAlreadyExistingMuleConfigWithApikitRouterWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistingMuleConfigWithApikitRouter();
  }

  public void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
    File xmlFile = getFile("scaffolder-existing/mule-config-no-api-flows-4.xml");
    List<File> xmls = Arrays.asList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, false, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(1, countOccurences(s, "<apikit:router config-ref=\"apikit-config\" />"));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(2, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(2, countOccurences(s, "get:\\:"));
    Collection<File> newXmlConfigs = FileUtils.listFiles(muleXmlOut, new String[] {"xml"}, true);
    assertEquals(0, newXmlConfigs.size());
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(3, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testMultipleMimeTypesWithoutNamedConfigWithOldParser() throws Exception {
    testMultipleMimeTypesWithoutNamedConfig();
  }

  @Test
  public void testMultipleMimeTypesWithoutNamedConfigWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testMultipleMimeTypesWithoutNamedConfig();
  }

  public void testMultipleMimeTypesWithoutNamedConfig() throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder/multipleMimeTypes.raml"));
    File muleXmlOut = folder.newFolder("scaffolder");
    List<File> xmls = Arrays.asList(getFile("scaffolder/multipleMimeTypes-4.xml"));

    createScaffolder(ramls, xmls, muleXmlOut, null, false, null).run();

    File muleXmlSimple = new File(muleXmlOut, "multipleMimeTypes-4.xml");
    assertTrue(muleXmlSimple.exists());

    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(8, countOccurences(s, "post:\\pet"));
    assertTrue(s.contains("post:\\pet:application\\json"));
    assertTrue(s.contains("post:\\pet:text\\xml"));
    assertTrue(s.contains("post:\\pet:application\\x-www-form-urlencoded"));
    assertFalse(s.contains("post:\\pet:application\\xml"));
    assertEquals(3, countOccurences(s, "post:\\vet"));
    assertFalse(s.contains("post:\\vet:application\\xml"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(0, countOccurences(s, "#[mel:null]"));
  }

  @Test
  public void testMultipleMimeTypesWithOldParser() throws Exception {
    testMultipleMimeTypes("multipleMimeTypes");
  }

  @Test
  public void testMultipleMimeTypesWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testMultipleMimeTypes("multipleMimeTypes");
  }

  @Test
  public void testMultipleMimeTypesV10() throws Exception {
    testMultipleMimeTypes("multipleMimeTypesV10");
  }

  private void testMultipleMimeTypes(String name) throws Exception {
    List<File> ramls = Arrays.asList(getFile("scaffolder/" + name + ".raml"));
    File muleXmlOut = folder.newFolder("scaffolder");

    createScaffolder(ramls, new ArrayList<File>(), muleXmlOut, null, false, null).run();

    File muleXmlSimple = new File(muleXmlOut, name + ".xml");
    assertTrue(muleXmlSimple.exists());

    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertTrue(s.contains("post:\\pet:application\\json:" + name + "-config"));
    assertTrue(s.contains("post:\\pet:text\\xml:" + name + "-config"));
    if (name.endsWith("V10")) {
      assertTrue(s.contains("post:\\pet:" + name + "-config"));
    } else {
      assertTrue(s.contains("post:\\pet:application\\x-www-form-urlencoded:" + name + "-config"));
    }
    assertTrue(s.contains("post:\\pet:" + name + "-config"));
    assertTrue(!s.contains("post:\\pet:application\\xml:" + name + "-config"));
    assertTrue(s.contains("post:\\vet:" + name + "-config"));
    assertTrue(!s.contains("post:\\vet:application\\xml:" + name + "-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
  }

  private File getFile(String s) throws Exception {
    if (s == null) {
      return null;
    }
    File file = folder.newFile(s);
    file.createNewFile();
    InputStream resourceAsStream = ScaffolderTest.class.getClassLoader().getResourceAsStream(s);
    IOUtils.copy(resourceAsStream,
                 new FileOutputStream(file));
    return file;
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      boolean compatibilityMode, Set<File> ramlsWithExtensionEnabled)
      throws FileNotFoundException {
    Log log = mock(Log.class);
    Map<File, InputStream> ramlMap = null;
    if (ramls != null) {
      ramlMap = getFileInputStreamMap(ramls);
    }
    Map<File, InputStream> xmlMap = getFileInputStreamMap(xmls);
    InputStream domainStream = null;
    if (domainFile != null) {
      domainStream = new FileInputStream(domainFile);
    }
    return new Scaffolder(log, muleXmlOut, ramlMap, xmlMap, domainStream, ramlsWithExtensionEnabled, DEFAULT_MULE_VERSION,
                          DEFAULT_RUNTIME_EDITION);
  }

  private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
    return fileListUtils.toStreamFromFiles(ramls);
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}
