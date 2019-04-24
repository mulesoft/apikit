/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.raml.implv2.ParserV2Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;

public class ScaffolderWithExistingConfigTest extends AbstractScaffolderTestCase {

  @Test
  public void testAlreadyExistsWithExtensionEnabledWithOldParser() throws Exception {
    testAlreadyExistsWithExtensionEnabled();
  }

  @Test
  public void testAlreadyExistsWithExtensionEnabledWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsWithExtensionEnabled();
  }

  @Test
  public void testAlreadyExistsWithExtensionEnabled() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-extension/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile = createTmpFile(tmpFile.getParentFile(), "scaffolder-existing-extension/simple-extension-enabled.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    Set<File> ramlwithEE = new TreeSet<>();
    ramlwithEE.add(tmpFile);
    createScaffolder(ramls, xmls, muleXmlOut, null, ramlwithEE).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
    assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(1, countOccurences(s, "get:\\\""));
    assertEquals(1, countOccurences(s, "extensionEnabled=\"true\""));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testAlreadyExistsOldGenerateWithOldParser() throws Exception {
    testAlreadyExistsOldGenerate();
  }

  @Test
  public void testAlreadyExistsOldGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsOldGenerate();
  }

  private void testAlreadyExistsOldGenerate() throws Exception {
    final File tmpFile = createTmpFile("scaffolder-existing-old/simple.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile = createTmpFile(tmpFile.getParentFile(), "scaffolder-existing-old/simple.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, null);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(0, countOccurences(s, "http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener"));
    assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));
    assertEquals(1, countOccurences(s, "get:\\pet"));
    assertEquals(2, countOccurences(s, "post:\\pet"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
  }


  @Test
  public void testAlreadyExistsOldWithAddressGenerateWithOldParser() throws Exception {
    testAlreadyExistsOldWithAddressGenerate();
  }

  @Test
  public void testAlreadyExistsOldWithAddressGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testAlreadyExistsOldWithAddressGenerate();
  }

  @Test
  @Ignore
  public void testAlreadyExistsOldWithAddressGenerate() throws Exception {

    final File tmpFile = createTmpFile("scaffolder-existing-old-address/complex.raml");
    List<File> ramls = singletonList(tmpFile);
    File xmlFile = createTmpFile(tmpFile.getParentFile(), "scaffolder-existing-old-address/complex.xml");
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, xmls, muleXmlOut, null, null).run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(0, countOccurences(s, "http:listener-config"));
    assertEquals(0, countOccurences(s, "http:listener"));
    assertEquals(1, countOccurences(s, "http:inbound-endpoint address"));
    //    assertEquals(1, countOccurences(s, "put:\\clients\\(clientId):complex-config"));
    assertEquals(1, countOccurences(s, "put:\\invoices\\(invoiceId):complex-config"));
    //    assertEquals(1, countOccurences(s, "put:\\items\\(itemId):application\\json:complex-config"));
    assertEquals(2, countOccurences(s, "put:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\clients\\(clientId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\invoices\\(invoiceId):complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\items\\(itemId):multipart\\form-data:complex-config"));
    assertEquals(2, countOccurences(s, "delete:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "get:\\:complex-config"));
    //    assertEquals(1, countOccurences(s, "get:\\clients\\(clientId):complex-config"));
    //    assertEquals(1, countOccurences(s, "get:\\clients:complex-config"));
    assertEquals(1, countOccurences(s, "get:\\invoices\\(invoiceId):complex-config"));
    assertEquals(1, countOccurences(s, "get:\\invoices:complex-config"));
    assertEquals(1, countOccurences(s, "get:\\items\\(itemId):complex-config"));
    assertEquals(1, countOccurences(s, "get:\\items:complex-config"));
    assertEquals(2, countOccurences(s, "get:\\providers\\(providerId):complex-config"));
    assertEquals(2, countOccurences(s, "get:\\providers:complex-config"));
    //    assertEquals(1, countOccurences(s, "post:\\clients:complex-config"));
    assertEquals(1, countOccurences(s, "post:\\invoices:complex-config"));
    assertEquals(2, countOccurences(s, "post:\\items:application\\json:complex-config"));
    assertEquals(2, countOccurences(s, "post:\\providers:complex-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    //    assertEquals(10, countOccurences(s, "<logger level=\"INFO\" message="));
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

  private void testMultipleMimeTypes(final String apiPah) throws Exception {

    List<File> ramls = singletonList(createTmpFile(apiPah));
    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, emptyList(), muleXmlOut, null, null).run();

    final String name = fileNameWhithOutExtension(apiPah);
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
