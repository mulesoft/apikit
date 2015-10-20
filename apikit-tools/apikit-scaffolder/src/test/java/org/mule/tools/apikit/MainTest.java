/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MainTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TemporaryFolder domainFolder = new TemporaryFolder();
    private File src;
    private File main;
    private File app;
    private File api;
    private File lala;
    private File project;
    private File apiFile;

    @Before
    public void setUp() throws Exception {
        project = folder.newFolder("my-project");
        src = new File(project, "src");
        main = new File(src, "main");
        app = new File(main, "app");
        api = new File(main, "api");
        lala = new File(api, "lala");

        api.mkdirs();
        app.mkdirs();
        lala.mkdirs();

        // Do
        apiFile = new File(api, "hello.raml");
        apiFile.createNewFile();
        new File(api, "bye.yml").createNewFile();
        new File(lala, "wow.raml").createNewFile();

        // Don't
        new File(main, "dont-read.raml").createNewFile();
    }

    @Test
    public void testGetIncludedFiles() throws Exception {
        Main main = new Main();
        List<String> files = main.getIncludedFiles(project, new String[] {"src/main/api/**/*.raml", "src/main/**/*.yml"},
                                                   new String[] {});
        HashSet<String> set = new HashSet<String>(files);

        assertTrue(set.contains(new File(project, "src/main/api/hello.raml").getAbsolutePath()));
        assertTrue(set.contains(new File(project, "src/main/api/bye.yml").getAbsolutePath()));
        assertTrue(set.contains(new File(project, "src/main/api/lala/wow.raml").getAbsolutePath()));
        assertFalse(set.contains(new File(project, "src/main/dont-read.raml").getAbsolutePath()));
        assertEquals(3, files.size());
    }
}
