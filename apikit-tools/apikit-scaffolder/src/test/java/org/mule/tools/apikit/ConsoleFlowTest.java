/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.apikit.Scaffolder;
import org.mule.tools.apikit.ScaffolderTest;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mule.tools.apikit.Helper.countOccurences;

public class ConsoleFlowTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private FileListUtils fileListUtils = new FileListUtils();

    @Before
    public void setUp() throws Exception {
        folder.newFolder("console-flow");
    }

    @Test
    public void testAlreadyExistWithConsoleEnabled() throws Exception {
        List<File> ramls = Arrays.asList(getFile("console-flow/simple-console.raml"));
        File xmlFile = getFile("console-flow/simple-enabled.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"true\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "get:/users"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(0, countOccurences(s, "<flow name=\"simple-enabled-console\">"));
        assertEquals(0, countOccurences(s, "apikit:console"));
    }

    @Test
    public void testAlreadyExistWithConsoleDisabled() throws Exception {
        List<File> ramls = Arrays.asList(getFile("console-flow/simple-console.raml"));
        File xmlFile = getFile("console-flow/simple-disabled.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "get:/users"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(0, countOccurences(s,"<flow name=\"simple-disabled-console\">"));
        assertEquals(0, countOccurences(s,"apikit:console"));
    }

    @Test
    public void testAlreadyExistWithConsoleDisabledAndConsoleFlow() throws Exception {
        List<File> ramls = Arrays.asList(getFile("console-flow/simple-console.raml"));
        File xmlFile = getFile("console-flow/simple-disabled-with-console.xml");
        List<File> xmls = Arrays.asList(xmlFile);
        File muleXmlOut = folder.newFolder("mule-xml-out");

        Scaffolder scaffolder = createScaffolder(ramls, xmls, muleXmlOut);
        scaffolder.run();

        assertTrue(xmlFile.exists());
        String s = IOUtils.toString(new FileInputStream(xmlFile));
        assertEquals(1, countOccurences(s, "http:listener-config name=\"HTTP_Listener_Configuration\" host=\"localhost\" port=\"${serverPort}\""));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\""));
        assertEquals(1, countOccurences(s, "consoleEnabled=\"false\""));
        assertEquals(0, countOccurences(s, "http:inbound-endpoint"));
        assertEquals(1, countOccurences(s, "get:/pet"));
        assertEquals(1, countOccurences(s, "post:/pet"));
        assertEquals(1, countOccurences(s, "get:/\""));
        assertEquals(1, countOccurences(s, "get:/users"));
        assertEquals(0, countOccurences(s, "extensionEnabled"));
        assertEquals(1, countOccurences(s, "http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/console/*\""));
        assertEquals(1, countOccurences(s, "apikit:console"));
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

    private Scaffolder createScaffolder(List<File> ramls, List<File> xmls, File muleXmlOut)
            throws FileNotFoundException
    {
        return createScaffolder(ramls, xmls, muleXmlOut, null, null, null);
    }

    private Map<File, InputStream> getFileInputStreamMap(List<File> ramls) {
        return fileListUtils.toStreamFromFiles(ramls);
    }
}
