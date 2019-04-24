package org.mule.tools.apikit;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.stubbing.Stubber;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.model.RuntimeEdition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_RUNTIME_EDITION;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class ScaffolderMule4Test extends AbstractScaffolderTestCase {


  @Test
  public void testSimpleGenerateV08() throws Exception {
    simpleGenerate("scaffolder/simple.raml");
  }

  @Test
  public void testSimpleGenerateV10() throws Exception {
    simpleGenerate("scaffolder/simpleV10.raml");
  }

  @Test
  public void testSimpleGenerateForCEV08() throws Exception {
    simpleGenerateForCE("scaffolder/simple.raml");
  }

  @Test
  public void testSimpleGenerateForCEV10() throws Exception {
    simpleGenerateForCE("scaffolder/simpleV10.raml");
  }

  @Test
  public void testSimpleGenerateWithExtensionWithOldParser() throws Exception {
    simpleGenerateWithExtension();
  }

  @Test
  public void generateWithIncludes08() throws Exception {
    String filepath = ScaffolderMule4Test.class.getClassLoader().getResource("scaffolder-include-08/api.raml").getFile();
    File file = new File(filepath);
    List<File> ramls = singletonList(file);
    File muleXmlOut = createTmpMuleXmlOutFolder();
    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, null);
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
    String filepath = ScaffolderMule4Test.class.getClassLoader().getResource("scaffolder-include-10/api.raml").getFile();
    File file = new File(filepath);
    List<File> ramls = singletonList(file);
    File muleXmlOut = createTmpMuleXmlOutFolder();
    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, null);
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
    simpleGenerateWithExtension();
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithOldParser() throws Exception {
    simpleGenerateWithExtensionInNull();
  }

  @Test
  public void testSimpleGenerateWithExtensionInNullWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithExtensionInNull();
  }

  @Test
  public void generateWithExamples() throws Exception {
    String filepath = ScaffolderMule4Test.class.getClassLoader()
        .getResource("scaffolder-with-examples/src/main/resources/api/api.raml").getFile();
    File file = new File(filepath);
    File muleXmlOut = createTmpMuleXmlOutFolder();
    Scaffolder scaffolder = createScaffolder(singletonList(file), emptyList(), muleXmlOut, null, emptySet(), null, EE);
    scaffolder.run();
    File xmlOut = new File(muleXmlOut, "api.xml");
    assertTrue(xmlOut.exists());
    String s = IOUtils.toString(new FileInputStream(xmlOut));
    assertNotNull(s);
    final String expected =
        IOUtils.toString(ScaffolderMule4Test.class.getClassLoader().getResourceAsStream("scaffolder-with-examples/api.xml"));
    assertEquals(expected, s);
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithOldParser() throws Exception {
    simpleGenerateWithListenerAndExtension();
  }

  @Test
  public void testSimpleGenerateWithListenerAndExtensionWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithListenerAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithOldParser() throws Exception {
    simpleGenerateWithCustomDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithCustomDomain();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithOldParser() throws Exception {
    simpleGenerateWithCustomDomainAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainAndExtensionWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithCustomDomainAndExtension();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithOldParser() throws Exception {
    simpleGenerateWithCustomDomainWithMultipleLC();
  }

  @Test
  public void testSimpleGenerateWithCustomDomainWithMultipleLCWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithCustomDomainWithMultipleLC();
  }


  @Test
  public void testSimpleGenerateWithEmptyDomainWithOldParser() throws Exception {
    simpleGenerateWithEmptyDomain();
  }

  @Test
  public void testSimpleGenerateWithEmptyDomainWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    simpleGenerateWithEmptyDomain();
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
    File muleXmlSimple = simpleGeneration("scaffolder/two.raml", null);
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
    nestedGenerate();
  }

  @Test
  public void testNestedGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    nestedGenerate();
  }

  @Test
  public void testSimpleGenerationWithRamlInsideAFolder() throws Exception {
    File xmlFile = createTmpFile("raml-inside-folder/api.xml");
    File ramlFile = createTmpFile("raml-inside-folder/folder/api.raml");
    List<File> ramls = singletonList(ramlFile);
    List<File> xmls = singletonList(xmlFile);
    File muleXmlOut = xmlFile.getParentFile();

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
    noNameGenerate();
  }

  @Test
  public void testNoNameGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    noNameGenerate();
  }

  @Test
  public void testExampleGenerateWithOldParser() throws Exception {
    exampleGenerate();
  }

  @Test
  public void testExampleGenerateWithNewParser() throws Exception {
    System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
    exampleGenerate();
  }

  @Test
  public void testExampleGenerateWithRamlType() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/example-v10.raml", null);
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
    final String apiPath = "scaffolder/example-v10.raml";
    File muleXmlSimple = simpleGeneration(apiPath, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
    assertTrue(muleXmlSimple.exists());
    String name = fileNameWhithOutExtension(apiPath);
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

  @Test
  public void testGenerateWithExchangeModules() throws Exception {

    final File tmpLibraryFolder = createTmpFile("scaffolder-exchange/exchange_modules/library1.raml").getParentFile();
    createTmpFile(tmpLibraryFolder, "scaffolder-exchange/exchange_modules/library2.raml");
    createTmpFile(tmpLibraryFolder, "scaffolder-exchange/exchange_modules/library3.raml");

    List<File> ramls = singletonList(createTmpFile(tmpLibraryFolder.getParentFile(), "scaffolder-exchange/api.raml"));

    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, null, DEFAULT_MULE_VERSION, EE);
    scaffolder.run();

    File muleXmlSimple = new File(muleXmlOut, "api.xml");

    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(1, countOccurences(s, "get:\\resource1:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource2:api-config"));
    assertEquals(1, countOccurences(s, "get:\\resource3:api-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithRecursiveApi() throws Exception {
    if (!isAmf())
      return;

    List<File> ramls = singletonList(createTmpFile("scaffolder/api-with-resource-type.raml"));

    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, null, DEFAULT_MULE_VERSION, EE);
    scaffolder.run();

    File muleXmlSimple = new File(muleXmlOut, "api-with-resource-type.xml");

    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "post:\\v4\\items:application\\json:api-with-resource-type-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithAMF() throws Exception {
    if (!isAmf())
      return;
    File muleXmlSimple = simpleGeneration("parser/amf-only.raml", null, DEFAULT_MULE_VERSION, EE);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\test:amf-only-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testGenerateWithRAML() throws Exception {
    if (isAmf())
      return;
    File muleXmlSimple = simpleGeneration("parser/raml-parser-only.raml", null, DEFAULT_MULE_VERSION, EE);
    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\test:raml-parser-only-config"));
    assertEquals(1, countOccurences(s, "<apikit:console"));
  }

  @Test
  public void testFailingGenerateWithBothParsers() throws Exception {
    final List<String> errors = new ArrayList<>();

    // create a logger that accumulates the errors instead of printing them out to standard output
    logger = mock(Log.class);
    final Stubber errorAccumulatorStubber = doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      errors.add(args[0].toString());
      return null;
    });
    errorAccumulatorStubber.when(logger).error(anyString());

    File muleXmlSimple = simpleGeneration("parser/failing-api.raml", null, DEFAULT_MULE_VERSION, EE);
    assertFalse(muleXmlSimple.exists());

    assertEquals(3, errors.size());
    assertTrue(errors.stream().anyMatch(e -> e.contains("Unresolved reference 'SomeTypo' from root context")));
  }

  @Test
  public void testGenerateFromTwoApis() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/simple/";

    testScaffoldTwoApis(testFolder, null);
  }

  @Test
  public void testGenerateFromTwoApisWithDomain() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/with-domain/";

    final File domainFile = createTmpFile(testFolder + "domains/mule-domain-config.xml");

    testScaffoldTwoApis(testFolder, domainFile);
  }

  @Test
  public void testGenerateFromTwoApisWithExistentConfig() throws Exception {
    final String testFolder = "scaffolder-from-two-apis/with-existent-config/";

    final String existentConfig = testFolder + "api.xml";

    testScaffoldTwoApis(testFolder, singletonList(existentConfig), null);
  }

  private void testScaffoldTwoApis(String testFolder, File domainFile) throws IOException {
    testScaffoldTwoApis(testFolder, emptyList(), domainFile);
  }

  private void testScaffoldTwoApis(String testFolder, List<String> existentConfigFileNames, File domainFile) throws IOException {
    final String basePath = testFolder + "src/main/resources/api/";
    final String api1 = basePath + "api1/api.raml";
    final String api2 = basePath + "api2/api.raml";
    File muleXmlOut = createTmpMuleXmlOutFolder();

    final List<File> existentConfigs = existentConfigFileNames.stream().map(c -> {
      try {
        return createTmpFile(muleXmlOut, c);
      } catch (IOException e) {
        throw new RuntimeException("Unexpected Error creating temporal config");
      }
    }).collect(toList());

    final List<File> ramls = new ArrayList<>();
    ramls.add(createTmpFile(api1));
    ramls.add(createTmpFile(api2));

    Scaffolder scaffolder = createScaffolder(ramls, existentConfigs, muleXmlOut, domainFile, emptySet(), null, EE);
    scaffolder.run();

    final File xmlOut1 = new File(muleXmlOut, "api.xml");
    assertTrue(xmlOut1.exists());
    final File xmlOut2 = new File(muleXmlOut, "api-2.xml");
    assertTrue(xmlOut2.exists());


    assertEquals(IOUtils.toString(ScaffolderMule4Test.class.getClassLoader().getResourceAsStream(testFolder + "api.xml")),
                 IOUtils.toString(new FileInputStream(xmlOut1)));

    assertEquals(IOUtils.toString(ScaffolderMule4Test.class.getClassLoader().getResourceAsStream(testFolder + "api-2.xml")),
                 IOUtils.toString(new FileInputStream(xmlOut2)));
  }


  private void simpleGenerateForCE(final String apiPath) throws Exception {
    File muleXmlSimple = simpleGeneration(apiPath, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);
    assertTrue(muleXmlSimple.exists());
    final String name = fileNameWhithOutExtension(apiPath);
    final String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
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

  private void simpleGenerate(final String apiPath) throws Exception {
    File muleXmlSimple = simpleGeneration(apiPath, null);
    assertTrue(muleXmlSimple.exists());
    final String name = fileNameWhithOutExtension(apiPath);
    final String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
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

  private void simpleGenerateWithExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("scaffolder/simple.raml", "scaffolder/simple.raml", null);
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
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithExtensionInNull() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("scaffolder/simple.raml", null, null);
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
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithListenerAndExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("scaffolder/simple.raml", "scaffolder/simple.raml", null);
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
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomain() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/simple.raml", "custom-domain-4/mule-domain-config.xml");
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
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomainAndExtension() throws Exception {
    File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("scaffolder/simple.raml", "scaffolder/simple.raml",
                                                                 "custom-domain-4/mule-domain-config.xml");
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
    assertEquals(1, countOccurences(s, "<apikit:console"));
    assertEquals(0, countOccurences(s, "consoleEnabled=\"false\""));
    assertEquals(5, countOccurences(s, "<logger level=\"INFO\" message="));
  }

  private void simpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/simple.raml", "custom-domain-multiple-lc-4/mule-domain-config.xml");
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

  private void simpleGenerateWithEmptyDomain() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/simple.raml", "empty-domain/mule-domain-config.xml");
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


  private void nestedGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/nested.raml", null);
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


  private void noNameGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/no-name.raml", null);
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

  private void exampleGenerate() throws Exception {
    File muleXmlSimple = simpleGeneration("scaffolder/example.raml", null);
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


  private void doubleRootRaml() throws Exception {

    final File tmpApiFile = createTmpFile("double-root-raml/simple.raml");
    List<File> ramls = Arrays.asList(tmpApiFile,
                                     createTmpFile(tmpApiFile.getParentFile(), "double-root-raml/two.raml"));

    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, null, null);
    scaffolder.run();

    File muleXmlSimple = new File(muleXmlOut, "simple.xml");
    File muleXmlTwo = new File(muleXmlOut, "two.xml");

    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "<http:listener-config"));
    assertEquals(2, countOccurences(s, "get:\\:simple-config"));
    assertEquals(2, countOccurences(s, "get:\\pet:simple-config"));
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

  private File simpleGeneration(final String apiPath, final String domainPath) throws Exception {
    return simpleGeneration(apiPath, domainPath, DEFAULT_MULE_VERSION, EE);
  }

  private File simpleGeneration(final String apiPath, final String domainPath, final String muleVersion,
                                final RuntimeEdition runtimeEdition)
      throws Exception {
    List<File> ramls = singletonList(createTmpFile(apiPath));
    File domainFile = domainPath == null ? null : createTmpFile(domainPath);

    File muleXmlOut = createTmpMuleXmlOutFolder();

    createScaffolder(ramls, emptyList(), muleXmlOut, domainFile, null, muleVersion, runtimeEdition).run();

    return new File(muleXmlOut, fileNameWhithOutExtension(apiPath) + ".xml");
  }

  @Test
  public void scaffoldEmptyAPI() throws Exception {
    File muleXmlOut = createTmpMuleXmlOutFolder();
    createScaffolder(singletonList(createTmpFile("scaffolder/without-resources.raml")), emptyList(),
                     muleXmlOut, null, null, DEFAULT_MULE_VERSION, EE).run();

    File api = new File(muleXmlOut, "without-resources.xml");

    assertEquals("Files are different", FileUtils
        .readFileToString(new File(getClass().getClassLoader().getResource("scaffolder/expected-result-without-resources.xml")
            .getFile()))
        .replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""),
                 FileUtils.readFileToString(api).replaceAll("api=(.*)raml\"", "api=\"\"").replaceAll("\\s+", ""));
  }

  private File simpleGenerationWithExtensionEnabled(final String apiPath, final String apiWithExtensionEnabledPath,
                                                    final String domainPath)
      throws Exception {
    final File apiTmpFile = createTmpFile(apiPath);
    List<File> ramls = singletonList(apiTmpFile);
    File domainFile = domainPath == null ? null : createTmpFile(domainPath);

    Set<File> ramlWithExtensionEnabled = null;
    if (apiWithExtensionEnabledPath != null) {
      ramlWithExtensionEnabled = new TreeSet<>();
      if (apiWithExtensionEnabledPath.equals(apiPath))
        ramlWithExtensionEnabled.add(apiTmpFile);
      else
        ramlWithExtensionEnabled.add(createTmpFile(apiWithExtensionEnabledPath));
    }

    File muleXmlOut = createTmpMuleXmlOutFolder();

    Scaffolder scaffolder = createScaffolder(ramls, emptyList(), muleXmlOut, domainFile, ramlWithExtensionEnabled);
    scaffolder.run();

    return muleXmlOut;
  }

}
