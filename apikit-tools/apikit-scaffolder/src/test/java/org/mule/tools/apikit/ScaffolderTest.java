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
import org.mockito.Mockito;

import org.mule.module.apikit.spi.ScaffolderService;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.apikit.Helper.*;

public class ScaffolderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private FileListUtils fileListUtils = new FileListUtils();


    @Before
    public void setUp() {
        folder.newFolder("scaffolder");
        folder.newFolder("scaffolder-existing");
        folder.newFolder("scaffolder-existing-custom-lc");
        folder.newFolder("scaffolder-existing-old");
        folder.newFolder("scaffolder-existing-old-address");
        folder.newFolder("scaffolder-existing-custom-and-normal-lc");
        folder.newFolder("custom-domain");
        folder.newFolder("empty-domain");
        folder.newFolder("custom-domain-multiple-lc");
    }

    @Test
    public void testSimpleGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", null, "3.7.0");
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithExtension() throws Exception {
        mockExtensionService();
        File muleXmlSimple = simpleGeneration("simple", null, "3.7.0");
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithInboundEndpoint() throws Exception {
        //File muleXmlSimple = simpleGeneration("simple");
        List<File> ramls = Arrays.asList(getFile("scaffolder/simple.raml"));
        List<File> xmls = Arrays.asList();
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.5.0");
        scaffolder.run();

        File xmlOut = new File(muleXmlOut, "simple.xml");

        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithListener() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder/simple.raml"));
        List<File> xmls = Arrays.asList();
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.6.0");
        scaffolder.run();

        File xmlOut = new File(muleXmlOut, "simple.xml");

        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(0, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithListenerAndExtension() throws Exception {
        //File muleXmlSimple = simpleGeneration("simple");
        List<File> ramls = Arrays.asList(getFile("scaffolder/simple.raml"));
        List<File> xmls = Arrays.asList();
        File muleXmlOut = folder.newFolder("mule-xml-out");

        mockExtensionService();

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, null, "3.7.0");
        scaffolder.run();

        File xmlOut = new File(muleXmlOut, "simple.xml");

        assertTrue(xmlOut.exists());
        String s = IOUtils.toString(new FileInputStream(xmlOut));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(0, countOccurences(s, "<http:inbound"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
    }


    @Test
    public void testSimpleGenerateWithCustomDomain() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "custom-domain/mule-domain-config.xml",null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithCustomDomainAndExtension() throws Exception {
        mockExtensionService();
        File muleXmlSimple = simpleGeneration("simple", "custom-domain/mule-domain-config.xml", "3.7.0");
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8081\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
        assertEquals(1, countOccurences(s, "extensionEnabled"));
    }

    @Test
    public void testSimpleGenerateWithCustomDomainWithMultipleLC() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "custom-domain-multiple-lc/mule-domain-config.xml", null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "config-ref=\"http-lc-0.0.0.0-8080\""));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
    }

    @Test
    public void testSimpleGenerateWithEmptyDomain() throws Exception {
        File muleXmlSimple = simpleGeneration("simple", "empty-domain/mule-domain-config.xml", null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "get:/:simple-config"));
        assertEquals(1, countOccurences(s, "get:/pet:simple-config"));
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
    public void testNoNameGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("no-name", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"no-name-httpListenerConfig\" host=\"0.0.0.0\" port=\"8081\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"no-name-httpListenerConfig\" path=\"/api/*\""));
    }

    @Test
    public void testExampleGenerate() throws Exception {
        File muleXmlSimple = simpleGeneration("example", null, null);
        assertTrue(muleXmlSimple.exists());
        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

        assertEquals(1, countOccurences(s, "{&#xA;    &quot;name&quot;: &quot;Bobby&quot;,&#xA;    &quot;food&quot;: &quot;Ice Cream&quot;&#xA;}"));
    }

    @Test
    public void testAlreadyExistsGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
        File xmlFile = getFile("scaffolder-existing/simple.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

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

    }

    @Test
    public void testAlreadyExistsGenerateWithCustomDomain() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-lc/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-custom-lc/simple.xml");
        File domainFile = getFile("custom-domain/mule-domain-config.xml");

        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");
        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut,domainFile);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "<http:listener-config"));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"http-lc-0.0.0.0-8081\" path=\"/api/*\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
    }

    @Test
    public void testAlreadyExistsGenerateWithCustomAndNormalLC() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.raml"));
        File xmlFile = getFile("scaffolder-existing-custom-and-normal-lc/leagues-custom-normal-lc.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");
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
    }

    @Test
    public void testAlreadyExistsOldGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-old/simple.raml"));
        File xmlFile = getFile("scaffolder-existing-old/simple.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(0, countOccurences(s, "http:listener-config"));
        assertEquals(0, countOccurences(s, "http:listener"));
        assertEquals(1, countOccurences(s, "http:inbound-endpoint port=\"${serverPort}\" host=\"localhost\" path=\"api\""));


        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
    }

    @Test
    public void testAlreadyExistingMuleConfigWithApikitRouter() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing/simple.raml"));
        File xmlFile = getFile("scaffolder-existing/mule-config-no-api-flows.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

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
    }

    @Test
    public void testAlreadyExistsOldWithAddressGenerate() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder-existing-old-address/complex.raml"));
        File xmlFile = getFile("scaffolder-existing-old-address/complex.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

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

    }

    @Test
    public void testMultipleMimeTypesWithoutNamedConfig() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder/multipleMimeTypes.raml"));
        File muleXmlOut = folder.newFolder("scaffolder");
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

    }

    @Test
    public void testMultipleMimeTypes() throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder/multipleMimeTypes.raml"));
        File muleXmlOut = folder.newFolder("scaffolder");

        createScaffolder(ramls, new ArrayList<File>(), muleXmlOut).run();

        File muleXmlSimple = new File(muleXmlOut, "multipleMimeTypes.xml");
        assertTrue(muleXmlSimple.exists());

        String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
        assertTrue(s.contains("post:/pet:application/json:multipleMimeTypes-config"));
        assertTrue(s.contains("post:/pet:text/xml:multipleMimeTypes-config"));
        assertTrue(s.contains("post:/pet:application/x-www-form-urlencoded:multipleMimeTypes-config"));
        assertTrue(s.contains("post:/pet:multipleMimeTypes-config"));
        assertTrue(!s.contains("post:/pet:application/xml:multipleMimeTypes-config"));

        assertTrue(s.contains("post:/vet:multipleMimeTypes-config"));
        assertTrue(!s.contains("post:/vet:application/xml:multipleMimeTypes-config"));
    }

    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut)
            throws FileNotFoundException
    {
        return createScaffolder(ramls, xmls, muleXmlOut, null, null);
    }
    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile) throws FileNotFoundException {
        return createScaffolder(ramls, xmls, muleXmlOut, domainFile, null);
    }
    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut, File domainFile, String muleVersion)
            throws FileNotFoundException {
        Log log = mock(Log.class);

        Map<File, InputStream> ramlMap = getFileInputStreamMap(ramls);
        Map<File, InputStream> xmlMap = getFileInputStreamMap(xmls);

        InputStream domainStream = null;
        if (domainFile != null)
        {
            domainStream = new FileInputStream(domainFile);
        }
        return new Scaffolder(log, muleXmlOut, ramlMap, xmlMap, domainStream, muleVersion);
    }

    private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
        return fileListUtils.toStreamFromFiles(ramls);
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

    private File simpleGeneration(String name, String domainPath, String muleVersion) throws Exception {
        List<File> ramls = Arrays.asList(getFile("scaffolder/" + name + ".raml"));
        File domainFile = getFile(domainPath);

        List<File> xmls = Arrays.asList();
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut, domainFile, muleVersion);
        scaffolder.run();

        return new File(muleXmlOut, name + ".xml");
    }

    private void mockExtensionService() throws NoSuchFieldException, IllegalAccessException
    {
        ScaffolderServiceLoader mockedLoader = Mockito.mock(ScaffolderServiceLoader.class);
        ScaffolderService mockedService = Mockito.mock(ScaffolderService.class);
        when(mockedLoader.loadService()).thenReturn(mockedService);
        Field loadService = ExtensionManager.class.getDeclaredField("serviceLoader");
        loadService.setAccessible(true);
        loadService.set(loadService, mockedLoader);
    }

    @After
    public void after() throws NoSuchFieldException, IllegalAccessException
    {
        Field loadService = ExtensionManager.class.getDeclaredField("serviceLoader");
        loadService.set(loadService, new ScaffolderServiceLoader());
        loadService.setAccessible(false);
    }
}
