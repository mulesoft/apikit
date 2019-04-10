/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.raml.implv2.ParserV2Utils;

public class ScaffolderWithExistingConfigMule4Test extends AbstractScaffolderTestCase {

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstFlowsXml() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-multiples/api.raml");
    List<File> ramls = singletonList(tmpFile);
    File resourcesFlows =
        createTmpFile(tmpFile.getParentFile(), "scaffolder-existing-multiples/resources-flows.xml");
    File noResourcesFlows =
        createTmpFile(
                      tmpFile.getParentFile(), "scaffolder-existing-multiples/no-resources-flows.xml");

    List<File> xmls = Arrays.asList(resourcesFlows, noResourcesFlows);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, xmls, muleXmlOut, null, null).run();

    assertTrue(resourcesFlows.exists());
    assertTrue(noResourcesFlows.exists());
    String s = IOUtils.toString(new FileInputStream(noResourcesFlows));
    assertEquals(2, countOccurences(s, "get:\\books"));
    assertEquals(2, countOccurences(s, "put:\\shows"));
    assertEquals(0, countOccurences(s, "patch:\\movies"));
  }

  @Test
  public void testAlreadyExistsMultipleConfigurationsFirstNoFlowsXml() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-multiples/api.raml");
    List<File> ramls = singletonList(tmpFile);
    File resourcesFlows =
        createTmpFile(tmpFile.getParentFile(), "scaffolder-existing-multiples/resources-flows.xml");
    File noResourcesFlows =
        createTmpFile(
                      tmpFile.getParentFile(), "scaffolder-existing-multiples/no-resources-flows.xml");

    List<File> xmls = Arrays.asList(noResourcesFlows, resourcesFlows);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, xmls, muleXmlOut, null, null).run();

    assertTrue(noResourcesFlows.exists());
    assertTrue(resourcesFlows.exists());
    String s = IOUtils.toString(new FileInputStream(noResourcesFlows));
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

  private void testAlreadyExistsWithExtensionDisabled() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-extension/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile =
        createTmpFile(
                      tmpFile.getParentFile(),
                      "scaffolder-existing-extension/simple-extension-disabled-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(tmpFile);
    createScaffolder(ramls, xmls, muleXmlOut, null, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(
                 1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(
                 1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(
                 1,
                 countOccurences(
                                 s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
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

  private void testAlreadyExistsWithExtensionEnabled() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-extension/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile =
        createTmpFile(
                      tmpFile.getParentFile(),
                      "scaffolder-existing-extension/simple-extension-enabled-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(tmpFile);
    createScaffolder(ramls, xmls, muleXmlOut, null, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(
                 1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(
                 1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(
                 1,
                 countOccurences(
                                 s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
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

  private void testAlreadyExistsWithExtensionNotPresent() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-extension/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile =
        createTmpFile(
                      tmpFile.getParentFile(),
                      "scaffolder-existing-extension/simple-extension-not-present-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(tmpFile);
    createScaffolder(ramls, xmls, muleXmlOut, null, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(
                 1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(
                 1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"${serverPort}\""));
    assertEquals(
                 1,
                 countOccurences(
                                 s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
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

  private void testAlreadyExistsGenerate() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile = createTmpFile(tmpFile.getParentFile(), "scaffolder-existing/simple-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, xmls, muleXmlOut, null, null).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(
                 1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(
                 1,
                 countOccurences(
                                 s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
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

  private void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-custom-lc/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile =
        createTmpFile(tmpFile.getParentFile(), ("scaffolder-existing-custom-lc/simple-4.xml"));
    File domainFile = createTmpFile("custom-domain-4/mule-domain-config.xml");

    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = folder.newFolder("mule-xml-out");
    createScaffolder(ramls, xmls, muleXmlOut, domainFile, null).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener-connection"));

    assertEquals(
                 1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
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

  private void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
    final File tmpFile =
        createTmpFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml");
    List<File> ramls = singletonList(tmpFile);

    File xmlFile =
        createTmpFile(
                      tmpFile.getParentFile(),
                      "scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();
    File domainFile = createTmpFile("custom-domain-4/mule-domain-config.xml");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(
                 1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "inbound-endpoint"));
    assertEquals(2, countOccurences(s, "get:\\leagues\\(leagueId)"));
    assertEquals(2, countOccurences(s, "post:\\leagues\\(leagueId)"));
    assertEquals(
                 1, countOccurences(s, "<http:listener config-ref=\"HTTP_Listener_Configuration\""));
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

  private void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile =
        createTmpFile(
                      tmpFile.getParentFile(), "scaffolder-existing/mule-config-no-api-flows-4.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(
                 1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\">"));
    assertEquals(
                 1,
                 countOccurences(
                                 s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
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

  private void testMultipleMimeTypesWithoutNamedConfig() throws Exception {
    final File tmpFile = createTmpFile("scaffolder/multipleMimeTypes.raml");
    List<File> ramls = singletonList(tmpFile);
    File muleXmlOut = createTmpMuleXmlOutFolder(tmpFile.getParentFile());
    List<File> xmls =
        singletonList(createTmpFile(tmpFile.getParentFile(), "scaffolder/multipleMimeTypes-4.xml"));

    createScaffolder(ramls, xmls, muleXmlOut, null, null).run();

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
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testMultipleMimeTypes("scaffolder/multipleMimeTypes.raml");
  }

  @Test
  public void testMultipleMimeTypesV10() throws Exception {
    testMultipleMimeTypes("scaffolder/multipleMimeTypesV10.raml");
  }

  private void testMultipleMimeTypes(String apiPath) throws Exception {
    List<File> ramls = singletonList(createTmpFile(apiPath));
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, emptyList(), muleXmlOut, null, null).run();

    final String name = fileNameWhithOutExtension(apiPath);
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
}
