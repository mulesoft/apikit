package org.mule.tools.apikit;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.RuntimeEdition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class ScaffolderMule4Test {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("scaffolder");
    folder.newFolder("scaffolder-exchange");
    folder.newFolder("scaffolder-exchange/exchange_modules");
    createFile("scaffolder-exchange/exchange_modules/library1.raml");
    createFile("scaffolder-exchange/exchange_modules/library2.raml");
    createFile("scaffolder-exchange/exchange_modules/library3.raml");
    folder.newFolder("double-root-raml");
    folder.newFolder("custom-domain-4");
    folder.newFolder("raml-inside-folder");
    folder.newFolder("raml-inside-folder/folder");
    folder.newFolder("empty-domain");
    folder.newFolder("custom-domain-multiple-lc-4");
  }

  @Test
  public void testSimpleGenerateV08() throws Exception {
    testSimpleGenerate("simple");
  }

  @Test
  public void testSimpleGenerateV10() throws Exception {
    testSimpleGenerate("simpleV10");
  }

  @Test
  public void testSimpleGenerateForCEV08() throws Exception {
    testSimpleGenerateForCE("simple");
  }

  @Test
  public void testSimpleGenerateForCEV10() throws Exception {
    testSimpleGenerateForCE("simpleV10");
  }

  public void testSimpleGenerateForCE(String name) throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder", name, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
    assertEquals(1, countOccurences(s, "http:listener-connection host=\"0.0.0.0\" port=\"8081\""));
    assertEquals(2, countOccurences(s, "http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "#[vars.outboundHeaders default {}]"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(0, countOccurences(s, "<ee:"));
    assertEquals(7,
                 countOccurences(s,
                                 "<set-variable variableName=\"outboundHeaders\" value=\"#[{'Content-Type':'application/json'}]\" />"));
    assertEquals(7, countOccurences(s, "<set-variable variableName=\"httpStatus\""));
    assertEquals(2,
                 countOccurences(s, "<set-variable value=\"#[attributes.uriParams.name]\" variableName=\"name\" />"));
    assertEquals(1,
                 countOccurences(s, "<set-variable value=\"#[attributes.uriParams.owner]\" variableName=\"owner\""));
    assertEquals(7, countOccurences(s, "<set-payload"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(8, countOccurences(s, "http:headers"));
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:" + name + "-config"));
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

  public void testSimpleGenerate(String name) throws Exception {
    File muleXmlSimple = simpleGeneration(name, null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"simple"));
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
    assertEquals(2, countOccurences(s, "get:\\:" + name + "-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:" + name + "-config"));
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

  @Test
  public void testSimpleGenerateWithExtensionWithOldParser() throws Exception {
    testSimpleGenerateWithExtension();
  }

  @Test
  public void generateWithIncludes08() throws Exception {
    String filepath = ScaffolderTest.class.getClassLoader().getResource("scaffolder-include-08/api.raml").getFile();
    File file = new File(filepath);
    List<File> ramls = asList(file);
    List<File> xmls = asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");
    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, null);
    scaffolder.run();
    File xmlOut = new File(muleXmlOut, "api.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertNotNull(s);
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "post:\\Queue:application\\json:api-config"));
    assertEquals(2, countOccurences(s, "post:\\Queue:text\\xml:api-config"));
    assertEquals(0, countOccurences(s, "#[NullPayload.getInstance()]"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));

  }

  @Test
  public void generateWithIncludes10() throws Exception {
    String filepath =
        ScaffolderTest.class.getClassLoader().getResource("scaffolder-include-10/api.raml").getFile();
    File file = new File(filepath);
    List<File> ramls = asList(file);
    List<File> xmls = asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");
    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, null);
    scaffolder.run();
    File xmlOut = new File(muleXmlOut, "api.xml");
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
    assertEquals(2, countOccurences(s, "post:\\Queue:application\\json:api-config"));
    assertEquals(2, countOccurences(s, "post:\\Queue:text\\xml:api-config"));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithExtensionWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithExtension();
  }

  public void testSimpleGenerateWithExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", null);
    File xmlOut = new File(muleXmlFolderOut, "simple.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithOldParser() throws Exception {
    testSimpleGenerateWithExtensionInNull();
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithExtensionInNull();
  }

  @Test
  public void generateWithExamples() throws Exception {
    String filepath =
        ScaffolderTest.class.getClassLoader().getResource("scaffolder-with-examples/src/main/resources/api/api.raml").getFile();
    File file = new File(filepath);
    File muleXmlOut = folder.newFolder("mule-xml-out");
    Scaffolder scaffolder = createScaffolder(singletonList(file), emptyList(), muleXmlOut, null, emptySet(), null, EE);
    scaffolder.run();
    File xmlOut = new File(muleXmlOut, "api.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertNotNull(s);
    final String expected =
        IOUtils.toString(ScaffolderTest.class.getClassLoader().getResourceAsStream("scaffolder-with-examples/api.xml"));
    assertEquals(expected, s);
  }


  public void testSimpleGenerateWithExtensionInNull() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", null, null);
    File xmlOut = new File(muleXmlFolderOut, "simple.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithOldParser() throws Exception {
    testSimpleGenerateWithListenerAndExtension();
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithListenerAndExtension();
  }

  public void testSimpleGenerateWithListenerAndExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", null);
    File xmlOut = new File(muleXmlFolderOut, "simple.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(0, countOccurences(s, "<http:inbound"));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithOldParser() throws Exception {
    testSimpleGenerateWithCustomDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithCustomDomain();
  }

  public void testSimpleGenerateWithCustomDomain() throws Exception {
    File muleXmlSimple = simpleGeneration("simple", "custom-domain-4/mule-domain-config.xml");
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "<http:listener "));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithOldParser() throws Exception {
    testSimpleGenerateWithCustomDomainAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithCustomDomainAndExtension();
  }

  public void testSimpleGenerateWithCustomDomainAndExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", "custom-domain-4/mule-domain-config.xml");
    File xmlOut = new File(muleXmlFolderOut, "simple.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithOldParser() throws Exception {
    testSimpleGenerateWithCustomDomainWithMultipleLC();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithCustomDomainWithMultipleLC();
  }

  public void testSimpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
    File muleXmlSimple = simpleGeneration("simple", "custom-domain-multiple-lc-4/mule-domain-config.xml");
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "config-ref=\"abcd\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerateWithEmptyDomainWithOldParser() throws Exception {
    testSimpleGenerateWithEmptyDomain();
  }

  @Test
  public void testSimpleGenerateWithEmptyDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testSimpleGenerateWithEmptyDomain();
  }

  public void testSimpleGenerateWithEmptyDomain() throws Exception {
    File muleXmlSimple = simpleGeneration("simple", "empty-domain/mule-domain-config.xml");
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(9, countOccurences(s, "<ee:variables>"));
    assertEquals(10, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\v1:simple-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testTwoResourceGenerateWithOldParser() throws Exception {
    testTwoResourceGenerate();
  }

  @Test
  public void testTwoResourceGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testTwoResourceGenerate();
  }

  @Test
  public void testTwoResourceGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("two", null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));

    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));

    assertEquals(2, countOccurences(s, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:two-config"));

    assertEquals(2, countOccurences(s, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s, "post:\\car:two-config"));

    assertEquals(4, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testNestedGenerateWithOldParser() throws Exception {
    testNestedGenerate();
  }

  @Test
  public void testNestedGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testNestedGenerate();
  }

  public void testNestedGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("nested", null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(4, countOccurences(s, "http:body"));
    assertEquals(2, countOccurences(s, "#[payload]"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "get:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\pet:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\pet\\owner:nested-config"));
    assertEquals(2, countOccurences(s, "get:\\car:nested-config"));
    assertEquals(2, countOccurences(s, "post:\\car:nested-config"));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  @Test
  public void testSimpleGenerationWithRamlInsideAFolder() throws Exception {
    File ramlFile = getFile("raml-inside-folder/folder/api.raml");
    List<File> ramls = asList(ramlFile);
    File xmlFile = getFile("raml-inside-folder/api.xml");
    List<File> xmls = asList(xmlFile);
    File muleXmlOut = folder.newFolder("raml-inside-folder");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
    scaffolder.run();

    assertTrue(xmlFile.exists());
    String s = IOUtils.toString(new FileInputStream(xmlFile));
    assertEquals(1, countOccurences(s, "<error-handler name="));
    assertEquals(1, countOccurences(s, "<flow name=\"post:\\oneResource:api-config\">"));
    assertEquals(1, countOccurences(s, "<http:listener-config name="));
  }

  @Test
  public void testNoNameGenerateWithOldParser() throws Exception {
    testNoNameGenerate();
  }

  @Test
  public void testNoNameGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testNoNameGenerate();
  }

  public void testNoNameGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("no-name", null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"no-name-httpListenerConfig\">"));
    assertEquals(1, countOccurences(s, "http:listener config-ref=\"no-name-httpListenerConfig\" path=\"/api/*\""));
  }

  @Test
  public void testExampleGenerateWithOldParser() throws Exception {
    testExampleGenerate();
  }

  @Test
  public void testExampleGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    testExampleGenerate();
  }

  public void testExampleGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("example", null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(9, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(9, countOccurences(s, "<ee:set-payload>"));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(8, countOccurences(s, "application/json"));
    assertEquals(1, countOccurences(s,
                                    "{\n" +
                                        "  name: \"Bobby\",\n" +
                                        "  food: \"Ice Cream\"\n" +
                                        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));
  }

  @Test
  public void testExampleGenerateWithRamlType() throws Exception {
    File muleXmlSimple = simpleGeneration("example-v10", null);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "{\n" +
        "  name: \"Bobby\",\n" +
        "  food: \"Ice Cream\"\n" +
        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));
    assertEquals(1, countOccurences(s, "{\n" +
        "  title: \"In Cold Blood\",\n" +
        "  author: \"Truman Capote\",\n" +
        "  year: 1966\n" +
        "}"));
  }


  @Test
  public void testExampleGenerateForCE() throws Exception {
    String name = "example-v10";
    File muleXmlSimple = simpleGeneration("scaffolder", name, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\pet:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  name: \"Bobby\",\n" +
        "  food: \"Ice Cream\"\n" +
        "}"));

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\person:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  Person: {\n" +
        "    name: \"Underwood\",\n" +
        "    address: \"Juana Manso 999\",\n" +
        "    country: \"Argentina\"\n" +
        "  }\n" +
        "}"));

    assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message=\"get:\\books:" + name + "-config\" />"));
    assertEquals(0, countOccurences(s, "{\n" +
        "  title: \"In Cold Blood\",\n" +
        "  author: \"Truman Capote\",\n" +
        "  year: 1966\n" +
        "}"));
  }

  @Test
  public void doubleRootRamlWithOldParser() throws Exception {
    doubleRootRaml();
  }

  @Test
  public void doubleRootRamlWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    doubleRootRaml();
  }

  public void doubleRootRaml() throws Exception {
    List<File> ramls = asList(getFile("double-root-raml/simple.raml"), getFile("double-root-raml/two.raml"));

    List<File> xmls = asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, null);
    scaffolder.run();

    File muleXmlSimple = new File(muleXmlOut, "simple.xml");
    File muleXmlTwo = new File(muleXmlOut, "two.xml");

    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(0, countOccurences(s, "interpretRequestErrors=\"true\""));
    assertEquals(2, countOccurences(s, "<logger level=\"INFO\" message="));
    assertEquals(2, countOccurences(s, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s, "<on-error-propagate"));
    assertEquals(7, countOccurences(s, "<ee:message>"));
    assertEquals(7, countOccurences(s, "<ee:variables>"));
    assertEquals(7, countOccurences(s, "<ee:set-variable"));
    assertEquals(7, countOccurences(s, "<ee:set-payload>"));

    String s2 = IOUtils.toString(new FileInputStream(muleXmlTwo));
    assertEquals(2, countOccurences(s2, "get:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\pet:two-config"));
    assertEquals(2, countOccurences(s2, "get:\\car:two-config"));
    assertEquals(2, countOccurences(s2, "post:\\car:two-config"));
    assertEquals(0, countOccurences(s2, "extensionEnabled"));
    assertEquals(0, countOccurences(s2, "interpretRequestErrors=\"true\""));
    assertEquals(4, countOccurences(s2, "<logger level=\"INFO\" message="));
    assertEquals(2, countOccurences(s2, "http:response statusCode=\"#[vars.httpStatus default 200]\""));
    assertEquals(2, countOccurences(s2, "http:error-response statusCode=\"#[vars.httpStatus default 500]\""));
    assertEquals(4, countOccurences(s2, "<http:headers>#[vars.outboundHeaders default {}]</http:headers>"));
    assertEquals(7, countOccurences(s2, "<on-error-propagate"));
    assertEquals(7, countOccurences(s2, "<ee:message>"));
    assertEquals(7, countOccurences(s2, "<ee:variables>"));
    assertEquals(7, countOccurences(s2, "<ee:set-variable"));
    assertEquals(7, countOccurences(s2, "<ee:set-payload>"));

  }

  @Test
  public void testGenerateWithExchangeModules() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder-exchange", "api", null, DEFAULT_MULE_VERSION, EE);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(1, countOccurences(s, "get:\\resource1:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource2:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource3:api-config"));
    assertEquals(0, countOccurences(s, "extensionEnabled"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Ignore
  @Test
  public void testGenerateFromTwoApis() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/";
    final String basePath = testFolder + "src/main/resources/api/";
    final String api1 =
        ScaffolderTest.class.getClassLoader().getResource(basePath + "api1/api.raml").getFile();
    final String api2 =
        ScaffolderTest.class.getClassLoader().getResource(basePath + "api2/api.raml").getFile();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    final List<File> ramls = new ArrayList<>();
    ramls.add(new File(api1));
    ramls.add(new File(api2));

    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, emptySet(), null, EE);
    scaffolder.run();

    final File xmlOut1 = new File(muleXmlOut, "api.xml");
    assertTrue(xmlOut1.exists());
    final File xmlOut2 = new File(muleXmlOut, "api-2.xml");
    assertTrue(xmlOut2.exists());


    assertEquals(IOUtils.toString(ScaffolderTest.class.getClassLoader().getResourceAsStream(testFolder + "api.xml")),
                 IOUtils.toString(new FileInputStream(xmlOut1)));

    assertEquals(IOUtils.toString(ScaffolderTest.class.getClassLoader().getResourceAsStream(testFolder + "api-2.xml")),
                 IOUtils.toString(new FileInputStream(xmlOut2)));
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, null, null);
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, domainFile, null);
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      Set<File> ramlsWithExtensionEnabled)
      throws FileNotFoundException {
    return createScaffolder(ramls, xmls, muleXmlOut, domainFile, ramlsWithExtensionEnabled, DEFAULT_MULE_VERSION, EE);
  }

  private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile,
                                      Set<File> ramlsWithExtensionEnabled, String muleVersion, RuntimeEdition runtimeEdition)
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
    return new Scaffolder(log, muleXmlOut, ramlMap, xmlMap, domainStream, ramlsWithExtensionEnabled, muleVersion, runtimeEdition);
  }

  private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
    return fileListUtils.toStreamFromFiles(ramls);
  }

  private File getFile(String s) throws Exception {
    if (s == null) {
      return null;
    }

    final File file = new File(s);
    if (file.exists())
      return file;
    else
      return createFile(s);
  }

  private File createFile(String s) throws IOException {
    File file = folder.newFile(s);
    file.createNewFile();
    InputStream resourceAsStream = ScaffolderTest.class.getClassLoader().getResourceAsStream(s);
    IOUtils.copy(resourceAsStream,
                 new FileOutputStream(file));
    return file;
  }

  private File simpleGeneration(String name, String domainPath) throws Exception {
    return simpleGeneration("scaffolder", name, domainPath, DEFAULT_MULE_VERSION, EE);
  }

  private File simpleGeneration(String apiPath, String name, String domainPath, String muleVersion, RuntimeEdition runtimeEdition)
      throws Exception {
    List<File> ramls = asList(getFile(apiPath + "/" + name + ".raml"));
    File domainFile = getFile(domainPath);

    List<File> xmls = asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, null, muleVersion, runtimeEdition);
    scaffolder.run();

    return new File(muleXmlOut, name + ".xml");
  }

  private File simpleGenerationWithExtensionEnabled(String raml, String ramlWithExtensionEnabledPath, String domainPath)
      throws Exception {
    List<File> ramlList = asList(getFile("scaffolder/" + raml + ".raml"));
    Set<File> ramlWithExtensionEnabled = null;
    if (ramlWithExtensionEnabledPath != null) {
      ramlWithExtensionEnabled = new TreeSet<>();
      ramlWithExtensionEnabled.add(getFile("scaffolder/" + ramlWithExtensionEnabledPath + ".raml"));
    }
    File domainFile = getFile(domainPath);

    List<File> xmls = asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    Scaffolder scaffolder = createScaffolder(ramlList, xmls, muleXmlOut, domainFile, ramlWithExtensionEnabled);
    scaffolder.run();

    return muleXmlOut;
  }

  @After
  public void after() {
    System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
  }
}

