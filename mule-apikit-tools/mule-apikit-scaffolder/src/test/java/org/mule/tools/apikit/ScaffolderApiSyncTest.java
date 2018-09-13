/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScaffolderApiSyncTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Test
  public void testSimpleGeneration() throws IOException {
    ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);
    final String exchangeJsonResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:exchange.json";
    final String rootRamlResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:simple.raml";


    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/exchange.json")).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(exchangeJsonResourceURL);
    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/example-v10.raml")).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(rootRamlResourceURL);

    List<Dependency> dependencyList = new ArrayList<>();
    dependencyList.add(createDependency("com.mycompany", "raml-api", "1.0.0", "raml", "zip"));
    List<File> xmls = Arrays.asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    new ScaffolderAPI().run(dependencyList, scaffolderResourceLoaderMock, muleXmlOut, null, null, null);
  }

  private InputStream getInputStream(String resourcePath) {
    InputStream resourceStream = null;

    try {
      resourceStream = new FileInputStream(resourcePath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return resourceStream;
  }

  private static Dependency createDependency(String groupId, String artifactId, String version, String classifier, String type) {


    Dependency dependency = new Dependency();

    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    dependency.setClassifier(classifier);
    dependency.setType(type);

    return dependency;
  }

}
