/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;

import com.google.common.io.PatternFilenameFilter;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ScaffolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private FileListUtils fileListUtils = new FileListUtils();

    @Before
    public void setUp() throws Exception {
        folder.newFolder("scaffolder");
        folder.newFolder("scaffolder-exchange");
        folder.newFolder("scaffolder-exchange/exchange_modules");
        createFile("scaffolder-exchange/exchange_modules/library1.raml");
        createFile("scaffolder-exchange/exchange_modules/library2.raml");
        createFile("scaffolder-exchange/exchange_modules/library3.raml");
        folder.newFolder("scaffolder-existing");
        folder.newFolder("scaffolder-existing-extension");
        folder.newFolder("scaffolder-existing-custom-lc");
        folder.newFolder("scaffolder-existing-old");
        folder.newFolder("scaffolder-existing-old-address");
        folder.newFolder("scaffolder-existing-custom-and-normal-lc");
        folder.newFolder("double-root-raml");
        folder.newFolder("raml-inside-folder");
        folder.newFolder("raml-inside-folder/folder");
        folder.newFolder("custom-domain");
        folder.newFolder("empty-domain");
        folder.newFolder("custom-domain-multiple-lc");
    }

    private File createFolder(String name) {
        try {
            return folder.newFolder(name);
        } catch(IOException e) {
            File[] files = folder.getRoot().listFiles();
            for (File file : files) {
                if (file.isDirectory() && file.getName().equals(name)) {
                    return file;
                }
            }
        }
        return null;
    }

    @Test
    public void testSimpleGenerateV08() throws Exception
    {
        testSimpleGenerate("simple");
    }

    @Test
    public void testSimpleGenerateV10() throws Exception
    {
        testSimpleGenerate("simpleV10");
    }

    public void testSimpleGenerate(String name) throws Exception {
        File muleXmlSimple = simpleGeneration(name, null, "3.8.0");
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:" + name + "-config"));
        assertEquals(1, countOccurences(s, "get:/pet:" + name + "-config"));
        assertEquals(1, countOccurences(s, "get:/pet/v1:" + name + "-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithExtensionWithOldParser() throws Exception
    {
        testSimpleGenerateWithExtension();
    }

    @Test
    public void generateWithIncludes08() throws Exception {
        String filepath = ScaffolderTest.class.getClassLoader().getResource("scaffolder-include-08/api.raml").getFile();
        File file = new File(filepath);
        List<File> ramls = Arrays.asList(file);
        List<File> xmls = Arrays.asList();
        File muleXmlOut = createFolder("mule-xml-out");
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.0", null);
        scaffolder.run();
        File xmlOut = new File (muleXmlOut, "api.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertNotNull(s);
        assertEquals(1, countOccurences(s, "post:/Queue:application/json:api-config"));
        assertEquals(1, countOccurences(s, "post:/Queue:text/xml:api-config"));
    }

    @Test
    public void generateWithIncludes10() throws Exception {
        String filepath = ScaffolderTest.class.getClassLoader().getResource("scaffolder-include-10/api.raml").getFile();
        File file = new File(filepath);
        List<File> ramls = Arrays.asList(file);
        List<File> xmls = Arrays.asList();
        File muleXmlOut = createFolder("mule-xml-out");
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.0", null);
        scaffolder.run();
        File xmlOut = new File (muleXmlOut, "api.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertNotNull(s);
        assertEquals(1, countOccurences(s, "post:/Queue:application/json:api-config"));
        assertEquals(1, countOccurences(s, "post:/Queue:text/xml:api-config"));
    }

    @Test
    public void testSimpleGenerateWithExtensionWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithExtension();
    }

    public void testSimpleGenerateWithExtension() throws Exception {
        File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", null, "3.8.0");
        File xmlOut = new File (muleXmlFolderOut, "simple.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithExtensionInNullWithOldParser() throws Exception
    {
        testSimpleGenerateWithExtensionInNull();
    }

    @Test
    public void testSimpleGenerateWithExtensionInNullWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithExtensionInNull();
    }

    public void testSimpleGenerateWithExtensionInNull() throws Exception {
        File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", null, null, "3.8.0");
        File xmlOut = new File (muleXmlFolderOut, "simple.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithInboundEndpointWithOldParser() throws Exception
    {
        testSimpleGenerateWithInboundEndpoint();
    }

    @Test
    public void testSimpleGenerateWithInboundEndpointWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithInboundEndpoint();
    }

    public void testSimpleGenerateWithInboundEndpoint() throws Exception {
        File xmlOut = simpleGeneration("simple", null, "3.5.0");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(2, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithListenerWithOldParser() throws Exception
    {
        testSimpleGenerateWithListener();
    }

    @Test
    public void testSimpleGenerateWithListenerWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithListener();
    }

    public void testSimpleGenerateWithListener() throws Exception {
        File xmlOut = simpleGeneration("simple", null, "3.6.0");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(0, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerationWithRamlInsideAFolder() throws Exception
    {
        File ramlFile = getFile("raml-inside-folder/folder/api.raml");
        List<File> ramls = Arrays.asList(ramlFile);
        File xmlFile = getFile("raml-inside-folder/api.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = null;

            muleXmlOut = createFolder("raml-inside-folder");
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "<apikit:mapping-exception-strategy name=\"api-apiKitGlobalExceptionMapping\">"));
        assertEquals(1, countOccurences(s, "<flow name=\"post:/oneResource:api-config\">"));
        assertEquals(1, countOccurences(s, "<http:listener-config name=\"api-httpListenerConfig\" host=\"0.0.0.0\" port=\"8081\" />"));
    }

    @Test
    public void testSimpleGenerateWithListenerAndExtensionWithOldParser() throws Exception
    {
        testSimpleGenerateWithListenerAndExtension();
    }

    @Test
    public void testSimpleGenerateWithListenerAndExtensionWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithListenerAndExtension();
    }

    public void testSimpleGenerateWithListenerAndExtension() throws Exception {
        File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", null, "3.8.0");
        File xmlOut = new File (muleXmlFolderOut, "simple.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(0, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithCustomDomainWithOldParser() throws Exception
    {
        testSimpleGenerateWithCustomDomain();
    }

    @Test
    public void testSimpleGenerateWithCustomDomainWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithCustomDomain();
    }

    public void testSimpleGenerateWithCustomDomain() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "custom-domain/mule-domain-config.xml",null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithCustomDomainAndExtensionWithOldParser() throws Exception
    {
        testSimpleGenerateWithCustomDomainAndExtension();
    }

    @Test
    public void testSimpleGenerateWithCustomDomainAndExtensionWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithCustomDomainAndExtension();
    }

    public void testSimpleGenerateWithCustomDomainAndExtension() throws Exception {
        File muleXmlFolderOut = simpleGenerationWithExtensionEnabled("simple", "simple", "custom-domain/mule-domain-config.xml", "3.8.0");
        File xmlOut = new File (muleXmlFolderOut, "simple.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(2, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithCustomDomainWithMultipleLCWithOldParser() throws Exception
    {
        testSimpleGenerateWithCustomDomainWithMultipleLC();
    }

    @Test
    public void testSimpleGenerateWithCustomDomainWithMultipleLCWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithCustomDomainWithMultipleLC();
    }

    public void testSimpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "custom-domain-multiple-lc/mule-domain-config.xml", null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(2, countOccurences(s, "config-ref=\"abcd\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testSimpleGenerateWithEmptyDomainWithOldParser() throws Exception
    {
        testSimpleGenerateWithEmptyDomain();
    }

    @Test
    public void testSimpleGenerateWithEmptyDomainWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testSimpleGenerateWithEmptyDomain();
    }

    public void testSimpleGenerateWithEmptyDomain() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "empty-domain/mule-domain-config.xml", null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void testTwoResourceGenerateWithOldParser() throws Exception
    {
        testTwoResourceGenerate();
    }

    @Test
    public void testTwoResourceGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testTwoResourceGenerate();
    }

    @Test
    public void testTwoResourceGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("two", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

        assertEquals(1, countOccurences(s, "get:/pet:two-config"));
        assertEquals(1, countOccurences(s, "post:/pet:two-config"));

        assertEquals(1, countOccurences(s, "get:/car:two-config"));
        assertEquals(1, countOccurences(s, "post:/car:two-config"));
    }

    @Test
    public void testNestedGenerateWithOldParser() throws Exception
    {
        testNestedGenerate();
    }

    @Test
    public void testNestedGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testNestedGenerate();
    }

    public void testNestedGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("nested", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "get:/pet:nested-config"));
        assertEquals(1, countOccurences(s, "post:/pet:nested-config"));
        assertEquals(1, countOccurences(s, "get:/pet/owner:nested-config"));
        assertEquals(1, countOccurences(s, "get:/car:nested-config"));
        assertEquals(1, countOccurences(s, "post:/car:nested-config"));
    }

    @Test
    public void testNoNameGenerateWithOldParser() throws Exception
    {
        testNoNameGenerate();
    }

    @Test
    public void testNoNameGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testNoNameGenerate();
    }

    public void testNoNameGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("no-name", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"no-name-httpListenerConfig\" host=\"0.0.0.0\" port=\"8081\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"no-name-httpListenerConfig\" path=\"/api/*\""));
    }

    @Test
    public void testExampleGenerateWithOldParser() throws Exception
    {
        testExampleGenerate();
    }

    @Test
    public void testExampleGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testExampleGenerate();
    }

    public void testExampleGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("example", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

        assertEquals(1, countOccurences(s, "{&#xA;    &quot;name&quot;: &quot;Bobby&quot;,&#xA;    &quot;food&quot;: &quot;Ice Cream&quot;&#xA;}"));
    }

    @Test
    public void testAlreadyExistsWithExtensionDisabledWithOldParser() throws Exception
    {
        testAlreadyExistsWithExtensionDisabled();
    }

    @Test
    public void testAlreadyExistsWithExtensionDisabledWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsWithExtensionDisabled();
    }

    public void testAlreadyExistsWithExtensionDisabled() throws Exception
    {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-extension/simple-extension-disabled.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Set<File> ramlwithEE = new TreeSet<>();
        ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
        createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.3", ramlwithEE).run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "extensionEnabled=\"false\""));
    }

    @Test
    public void testAlreadyExistsWithExtensionEnabledWithOldParser() throws Exception
    {
        testAlreadyExistsWithExtensionEnabled();
    }

    @Test
    public void testAlreadyExistsWithExtensionEnabledWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsWithExtensionEnabled();
    }

    @Test
    public void testAlreadyExistsWithExtensionEnabled() throws Exception
    {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-extension/simple-extension-enabled.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Set<File> ramlwithEE = new TreeSet<>();
        ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
        createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.3", ramlwithEE).run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "extensionEnabled=\"true\""));
    }

    @Test
    public void testAlreadyExistsWithExtensionNotPresentWithOldParser() throws Exception
    {
        testAlreadyExistsWithExtensionNotPresent();
    }

    @Test
    public void testAlreadyExistsWithExtensionNotPresentWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsWithExtensionNotPresent();
    }

    public void testAlreadyExistsWithExtensionNotPresent() throws Exception
    {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-extension/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-extension/simple-extension-not-present.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Set<File> ramlwithEE = new TreeSet<>();
        ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
        createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.3", ramlwithEE).run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistsGenerateWithOldParser() throws Exception
    {
        testAlreadyExistsGenerate();
    }

    @Test
    public void testAlreadyExistsGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsGenerate();
    }

    public void testAlreadyExistsGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
        File xmlFile = getFile("scaffolder-existing/simple.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistsGenerateWithCustomDomainWithOldParser() throws Exception
    {
        testAlreadyExistsGenerateWithCustomDomain();
    }

    @Test
    public void testAlreadyExistsGenerateWithCustomDomainWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsGenerateWithCustomDomain();
    }

    public void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-lc/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-custom-lc/simple.xml");
        File domainFile = getFile("custom-domain/mule-domain-config.xml");

        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut,domainFile);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistsGenerateWithCustomAndNormalLCWithOldParser() throws Exception
    {
        testAlreadyExistsGenerateWithCustomAndNormalLC();
    }

    @Test
    public void testAlreadyExistsGenerateWithCustomAndNormalLCWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsGenerateWithCustomAndNormalLC();
    }

    public void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml"));
        File xmlFile = getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");
        File domainFile = getFile("custom-domain/mule-domain-config.xml");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/leagues/{leagueId}"));
        assertEquals(1, countOccurences(s, "<http:listener config-ref=\"HTTP_Listener_Configuration\""));
        assertEquals(1, countOccurences(s, "<http:listener config-ref=\"http-lc-0.0.0.0-8081\""));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistsOldGenerateWithOldParser() throws Exception
    {
        testAlreadyExistsOldGenerate();
    }

    @Test
    public void testAlreadyExistsOldGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsOldGenerate();
    }

    public void testAlreadyExistsOldGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-old/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-old/simple.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "http:listener-config"));
        assertEquals(0, countOccurences(s, "http:listener"));
        assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistingMuleConfigWithApikitRouterWithOldParser() throws Exception
    {
        testAlreadyExistingMuleConfigWithApikitRouter();
    }

    @Test
    public void testAlreadyExistingMuleConfigWithApikitRouterWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistingMuleConfigWithApikitRouter();
    }

    public void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
        File xmlFile = getFile("scaffolder-existing/mule-config-no-api-flows.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(1, countOccurences(s, "<apikit:router config-ref=\"apikit-config\" />"));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/:"));
        Collection<File> newXmlConfigs = FileUtils.listFiles(muleXmlOut, new String[] {"xml"}, true);
        assertEquals(0, newXmlConfigs.size());
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testAlreadyExistsOldWithAddressGenerateWithOldParser() throws Exception
    {
        testAlreadyExistsOldWithAddressGenerate();
    }

    @Test
    public void testAlreadyExistsOldWithAddressGenerateWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testAlreadyExistsOldWithAddressGenerate();
    }

    @Test
    public void testAlreadyExistsOldWithAddressGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-old-address/complex.raml"));
        File xmlFile = getFile("scaffolder-existing-old-address/complex.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "http:listener-config"));
        assertEquals(0, countOccurences(s, "http:listener"));
        assertEquals(1, countOccurences(s, "http:inbound-endpoint address"));
        assertEquals(1, countOccurences(s, "put:/clients/{clientId}:complex-config"));
        assertEquals(1, countOccurences(s, "put:/invoices/{invoiceId}:complex-config"));
        assertEquals(1, countOccurences(s, "put:/items/{itemId}:application/json:complex-config"));
        assertEquals(1, countOccurences(s, "put:/providers/{providerId}:complex-config"));
        assertEquals(1, countOccurences(s, "delete:/clients/{clientId}:complex-config"));
        assertEquals(1, countOccurences(s, "delete:/invoices/{invoiceId}:complex-config"));
        assertEquals(1, countOccurences(s, "delete:/items/{itemId}:multipart/form-data:complex-config"));
        assertEquals(1, countOccurences(s, "delete:/providers/{providerId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/:complex-config"));
        assertEquals(1, countOccurences(s, "get:/clients/{clientId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/clients:complex-config"));
        assertEquals(1, countOccurences(s, "get:/invoices/{invoiceId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/invoices:complex-config"));
        assertEquals(1, countOccurences(s, "get:/items/{itemId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/items:complex-config"));
        assertEquals(1, countOccurences(s, "get:/providers/{providerId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/providers:complex-config"));
        assertEquals(1, countOccurences(s, "post:/clients:complex-config"));
        assertEquals(1, countOccurences(s, "post:/invoices:complex-config"));
        assertEquals(1, countOccurences(s, "post:/items:application/json:complex-config"));
        assertEquals(1, countOccurences(s, "post:/providers:complex-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testMultipleMimeTypesWithoutNamedConfigWithOldParser() throws Exception
    {
        testMultipleMimeTypesWithoutNamedConfig();
    }

    @Test
    public void testMultipleMimeTypesWithoutNamedConfigWithNewParser() throws Exception
    {
        System.setProperty(ParserV2Utils.PARSER_V2_PROPERTY, "true");
        testMultipleMimeTypesWithoutNamedConfig();
    }

    public void testMultipleMimeTypesWithoutNamedConfig() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder/multipleMimeTypes.raml"));
        File muleXmlOut = createFolder("scaffolder");
        List<File> xmls = Arrays.asList(getFile("scaffolder/multipleMimeTypes.xml"));

        createScaffolder(ramls, xmls, muleXmlOut, null).run();

        File muleXmlSimple = new File(muleXmlOut, "multipleMimeTypes.xml");
        assertTrue(muleXmlSimple.exists());

        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertTrue(s.contains("post:/pet:application/json"));
        assertTrue(s.contains("post:/pet:text/xml"));
        assertTrue(s.contains("post:/pet:application/x-www-form-urlencoded"));
        assertTrue(s.contains("post:/pet"));
        assertTrue(!s.contains("post:/pet:application/xml"));
        assertTrue(s.contains("post:/vet"));
        assertTrue(!s.contains("post:/vet:application/xml"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
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
        File muleXmlOut = createFolder("scaffolder");

        createScaffolder(ramls, new ArrayList<File>(), muleXmlOut).run();

        File muleXmlSimple = new File(muleXmlOut, name + ".xml");
        assertTrue(muleXmlSimple.exists());

        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertTrue(s.contains("post:/pet:application/json:" + name + "-config"));
        assertTrue(s.contains("post:/pet:text/xml:" + name + "-config"));
        if (name.endsWith("V10"))
        {
            assertTrue(s.contains("post:/pet:" + name + "-config"));
        }
        else
        {
            assertTrue(s.contains("post:/pet:application/x-www-form-urlencoded:" + name + "-config"));
        }
        assertTrue(s.contains("post:/pet:" + name + "-config"));
        assertTrue(!s.contains("post:/pet:application/xml:" + name + "-config"));
        assertTrue(s.contains("post:/vet:" + name + "-config"));
        assertTrue(!s.contains("post:/vet:application/xml:" + name + "-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
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
    public void testGenerateWithExchangeModules() throws Exception
    {
        File muleXmlSimple = simpleGeneration("scaffolder-exchange", "api", null, "3.8.0");
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/resource1:api-config"));
        assertEquals(1, countOccurences(s, "get:/resource2:api-config"));
        assertEquals(1, countOccurences(s, "get:/resource3:api-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<apikit:console"));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
    }

    @Test
    public void generateWithExamples() throws Exception {
        String filepath = ScaffolderTest.class.getClassLoader().getResource("scaffolder-with-examples/api.raml").getFile();
        File file = new File(filepath);
        File muleXmlOut = createFolder("mule-xml-out");
        final List<File> ramls = singletonList(file);
        final List<File> xmls = emptyList();
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();
        File xmlOut = new File(muleXmlOut, "api.xml");
        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertNotNull(s);
        final String expected =
                IOUtils.toString(ScaffolderTest.class.getClassLoader().getResourceAsStream("scaffolder-with-examples/api.xml"));
        assertEquals(expected, s);
    }

    public void doubleRootRaml() throws Exception
    {
        List<File> ramls = Arrays.asList(getFile("double-root-raml/simple.raml"), getFile("double-root-raml/two.raml"));

        List<File> xmls = Arrays.asList();
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.3", null);
        scaffolder.run();

        File muleXmlSimple = new File(muleXmlOut, "simple.xml");
        File muleXmlTwo = new File(muleXmlOut, "two.xml");

        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));

        String s2 = IOUtils.toString(new FileInputStream(muleXmlTwo));
        assertEquals(1, countOccurences(s2, "get:/pet:two-config"));
        assertEquals(1, countOccurences(s2, "post:/pet:two-config"));
        assertEquals(1, countOccurences(s2, "get:/car:two-config"));
        assertEquals(1, countOccurences(s2, "post:/car:two-config"));
        assertEquals(0, countOccurences(s2, "extensionEnabled"));
    }

    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut)
            throws FileNotFoundException
    {
        return createScaffolder(ramls, xmls, muleXmlOut, null, null, null);
    }
    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile) throws FileNotFoundException {
        return createScaffolder(ramls, xmls, muleXmlOut, domainFile, null, null);
    }
    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile, String muleVersion, Set<File> ramlsWithExtensionEnabled)
            throws FileNotFoundException {
        Log log = mock(Log.class);
        Map<File, InputStream> ramlMap = null;
        if (ramls != null)
        {
            ramlMap = getFileInputStreamMap(ramls);
        }
        Map<File, InputStream> xmlMap = getFileInputStreamMap(xmls);
        InputStream domainStream = null;
        if (domainFile != null)
        {
            domainStream = new FileInputStream(domainFile);
        }
        return new Scaffolder(log, muleXmlOut, ramlMap, xmlMap, domainStream, muleVersion, ramlsWithExtensionEnabled);
    }

    private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
        return fileListUtils.toStreamFromFiles(ramls);
    }

    private File getFile(String s) throws  Exception {
        if (s == null)
        {
            return null;
        }

        final File file = new File(s);
        if (file.exists()) return file;
        else return createFile(s);
    }

    private File lookForExistingFile(File parentFolder, String fileRelPath) {
        File[] files = parentFolder.listFiles(new PatternFilenameFilter(fileRelPath));
        if (files.length == 1) {
            return files[0];
        }
        String[] pathSegments = fileRelPath.split("/");
        for (File file : parentFolder.listFiles()) {
            if (file.isDirectory() && file.getName().equals(pathSegments[0])) {
                return lookForExistingFile(file, fileRelPath.substring(fileRelPath.indexOf("/") + 1));
            }
        }
        return null;
    }

    private File createFile(String s) throws IOException {
        File file;
        try {
            file = folder.newFile(s);
            file.createNewFile();
        } catch (IOException e) {
            file = lookForExistingFile(folder.getRoot(),s);
        }
        if (file != null) {
            InputStream resourceAsStream = ScaffolderTest.class.getClassLoader().getResourceAsStream(s);
            IOUtils.copy(resourceAsStream,
                    new FileOutputStream(file));
        }
        return file;
    }

    private File simpleGeneration(String name, String domainPath, String muleVersion) throws Exception
    {
        return simpleGeneration("scaffolder", name, domainPath, muleVersion);
    }

    private File simpleGeneration(String apiPath, String name, String domainPath, String muleVersion) throws Exception {
        List<File> ramls = Arrays.asList(getFile(apiPath + "/" + name + ".raml"));
        File domainFile = getFile(domainPath);

        List<File> xmls = Arrays.asList();
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, muleVersion, null);
        scaffolder.run();

        return new File(muleXmlOut, name + ".xml");
    }

    private File simpleGenerationWithExtensionEnabled(String raml, String ramlWithExtensionEnabledPath, String domainPath, String muleVersion) throws Exception
    {
        List<File> ramlList = Arrays.asList(getFile("scaffolder/" + raml + ".raml"));
        Set<File> ramlWithExtensionEnabled = null;
        if (ramlWithExtensionEnabledPath != null)
        {
            ramlWithExtensionEnabled = new TreeSet<>();
            ramlWithExtensionEnabled.add(getFile("scaffolder/" + ramlWithExtensionEnabledPath + ".raml"));
        }
        File domainFile = getFile(domainPath);

        List<File> xmls = Arrays.asList();
        File muleXmlOut = createFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramlList, xmls, muleXmlOut, domainFile, muleVersion, ramlWithExtensionEnabled);
        scaffolder.run();

        return muleXmlOut;
    }

    @After
    public void after()
    {
        System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
    }
}
