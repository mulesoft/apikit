/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.Scaffolder.DEFAULT_MULE_VERSION;

import org.mule.raml.implv2.ParserV2Utils;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ScaffolderWithExistingConfig
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private FileListUtils fileListUtils = new FileListUtils();

    @Before
    public void setUp() {
        folder.newFolder("scaffolder");
        folder.newFolder("scaffolder-existing-extension");
        folder.newFolder("scaffolder-existing-old");
        folder.newFolder("scaffolder-existing-old-address");
        folder.newFolder("custom-domain");
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
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Set<File> ramlwithEE = new TreeSet<>();
        ramlwithEE.add(getFile("scaffolder-existing-extension/simple.raml"));
        createScaffolder(ramls, xmls, muleXmlOut, null, false, ramlwithEE).run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(2, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "extensionEnabled=\"true\""));
        assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
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
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, true, null);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "http:listener-config"));
        assertEquals(0, countOccurences(s, "http:listener"));
        assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(2, countOccurences(s, "post:/pet"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "<logger level=\"INFO\" message="));
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
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, true, null);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "http:listener-config"));
        assertEquals(0, countOccurences(s, "http:listener"));
        assertEquals(1, countOccurences(s, "http:inbound-endpoint address"));
        assertEquals(2, countOccurences(s, "put:/clients/{clientId}:complex-config"));
        assertEquals(1, countOccurences(s, "put:/invoices/{invoiceId}:complex-config"));
        assertEquals(2, countOccurences(s, "put:/items/{itemId}:application/json:complex-config"));
        assertEquals(2, countOccurences(s, "put:/providers/{providerId}:complex-config"));
        assertEquals(2, countOccurences(s, "delete:/clients/{clientId}:complex-config"));
        assertEquals(2, countOccurences(s, "delete:/invoices/{invoiceId}:complex-config"));
        assertEquals(2, countOccurences(s, "delete:/items/{itemId}:multipart/form-data:complex-config"));
        assertEquals(2, countOccurences(s, "delete:/providers/{providerId}:complex-config"));
        assertEquals(2, countOccurences(s, "get:/:complex-config"));
        assertEquals(2, countOccurences(s, "get:/clients/{clientId}:complex-config"));
        assertEquals(2, countOccurences(s, "get:/clients:complex-config"));
        assertEquals(1, countOccurences(s, "get:/invoices/{invoiceId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/invoices:complex-config"));
        assertEquals(1, countOccurences(s, "get:/items/{itemId}:complex-config"));
        assertEquals(1, countOccurences(s, "get:/items:complex-config"));
        assertEquals(2, countOccurences(s, "get:/providers/{providerId}:complex-config"));
        assertEquals(2, countOccurences(s, "get:/providers:complex-config"));
        assertEquals(2, countOccurences(s, "post:/clients:complex-config"));
        assertEquals(1, countOccurences(s, "post:/invoices:complex-config"));
        assertEquals(2, countOccurences(s, "post:/items:application/json:complex-config"));
        assertEquals(2, countOccurences(s, "post:/providers:complex-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(15, countOccurences(s, "<logger level=\"INFO\" message="));
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

    private File getFile(String s) throws  Exception {
        if (s == null)
        {
            return null;
        }
        File file = folder.newFile(s);
        file.createNewFile();
        InputStream resourceAsStream = ScaffolderTest.class.getClassLoader().getResourceAsStream(s);
        IOUtils.copy(resourceAsStream,
                     new FileOutputStream(file));
        return file;
    }

    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile, boolean compatibilityMode, Set<File> ramlsWithExtensionEnabled)
            throws FileNotFoundException
    {
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
        return new Scaffolder(log, muleXmlOut, ramlMap, xmlMap, domainStream, ramlsWithExtensionEnabled, DEFAULT_MULE_VERSION);
    }

    private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
        return fileListUtils.toStreamFromFiles(ramls);
    }

    @After
    public void after()
    {
        System.clearProperty(ParserV2Utils.PARSER_V2_PROPERTY);
    }
}
