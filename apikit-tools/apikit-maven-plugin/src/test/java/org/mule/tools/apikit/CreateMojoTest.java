/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;


@RunWith(JUnit4.class)
public class CreateMojoTest extends AbstractMojoTestCase {
    private CreateMojo mojo;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File src;
    private File main;
    private File app;
    private File api;
    private File lala;
    private File project;
    private File apiFile;

    @Before
    public void setUp() throws Exception {
        mojo = new CreateMojo();


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
        apiFile = new File(api, "hello.yaml");
        apiFile.createNewFile();
        new File(api, "bye.yml").createNewFile();
        new File(lala, "wow.yaml").createNewFile();

        // Don't
        new File(main, "dont-read.yaml").createNewFile();

        // TODO mock properties like this:
        setVariableValueToObject(mojo, "buildContext", new DefaultBuildContext());
        setVariableValueToObject(mojo, "log", mock(Log.class));
    }

    @Test
    public void testGetIncludedFiles() throws Exception {
        List<String> files = mojo.getIncludedFiles(project, new String[]{"src/main/api/**/*.yaml", "src/main/**/*.yml"},
                new String[]{});
        HashSet<String> set = new HashSet<String>(files);

        assertTrue(set.contains(new File(project, "src/main/api/hello.yaml").getAbsolutePath()));
        assertTrue(set.contains(new File(project, "src/main/api/bye.yml").getAbsolutePath()));
        assertTrue(set.contains(new File(project, "src/main/api/lala/wow.yaml").getAbsolutePath()));
        assertFalse(set.contains(new File(project, "src/main/dont-read.yaml").getAbsolutePath()));
        assertEquals(3, files.size());
    }

    @Test
    public void testExecute() throws  Exception {
        setVariableValueToObject(mojo, "muleXmlDirectory", app);
        setVariableValueToObject(mojo, "specDirectory", project);
        setVariableValueToObject(mojo, "muleXmlOutputDirectory", app);

        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("create-mojo/simple.yaml"),
                new FileOutputStream(apiFile));

        mojo.execute();

        assertTrue(apiFile.exists());
        FileInputStream input = new FileInputStream(apiFile);
        String ramlFileContent = IOUtils.toString(input);
        input.close();

        assertTrue(ramlFileContent.length() > 0);
        File muleConfigFile = new File (project.getPath() + "/src/main/app/hello.xml");
        assertTrue(muleConfigFile.exists());

        input = new FileInputStream(muleConfigFile);
        String muleConfigContent = IOUtils.toString(input);
        input.close();

        assertTrue(muleConfigContent.length() > 0);
        assertTrue(muleConfigContent.contains("listener"));

    }


    @Test
    public void testExecuteComplexRaml() throws  Exception {
        setVariableValueToObject(mojo, "muleXmlDirectory", app);
        setVariableValueToObject(mojo, "specDirectory", project);
        setVariableValueToObject(mojo, "muleXmlOutputDirectory", app);

        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("create-mojo/complex.raml"),
                     new FileOutputStream(apiFile));

        mojo.execute();

        assertTrue(apiFile.exists());
        FileInputStream input = new FileInputStream(apiFile);
        String ramlFileContent = IOUtils.toString(input);
        input.close();

        assertTrue(ramlFileContent.length() > 0);
        File muleConfigFile = new File (project.getPath() + "/src/main/app/hello.xml");
        assertTrue(muleConfigFile.exists());

        input = new FileInputStream(muleConfigFile);
        String muleConfigContent = IOUtils.toString(input);
        input.close();

        assertTrue(muleConfigContent.length() > 0);
        assertTrue(muleConfigContent.contains("listener"));

    }
}
