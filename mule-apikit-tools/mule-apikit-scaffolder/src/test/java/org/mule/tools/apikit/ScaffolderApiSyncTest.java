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
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.model.ScaffolderResourceLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mule.tools.apikit.Helper.countOccurences;
import static org.mule.tools.apikit.model.RuntimeEdition.CE;
import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class ScaffolderApiSyncTest {
  public final static String DEFAULT_MULE_VERSION = "4.0.0";
  public final static RuntimeEdition DEFAULT_RUNTIME_EDITION = EE;
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Test
  public void testSimpleGeneration() throws IOException {
    ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);
    final String exchangeJsonResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:exchange.json";
    final String rootRamlResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:simpleV10.raml";

    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/exchange2.json")).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(exchangeJsonResourceURL);
    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/simpleV10.raml")).when(scaffolderResourceLoaderMock)
        .getResourceAsStream(rootRamlResourceURL);

    List<Dependency> dependencyList = new ArrayList<>();
    dependencyList.add(createDependency("com.mycompany", "raml-api", "1.0.0", "raml", "zip"));
    List<File> xmls = Arrays.asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    new ScaffolderAPI().run(dependencyList, scaffolderResourceLoaderMock, muleXmlOut, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);

    File muleXmlSimple = new File(muleXmlOut, "resource::com.mycompany:raml-api:1.0.0:raml:zip:simpleV10.xml");

    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));

    assertEquals(1, countOccurences(s, "http:listener-config name=\"resource::com.mycompany:raml-api:1.0.0:raml:zip:simpleV10"));
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
//    assertEquals(2, countOccurences(s, "get:\\:" + rootRamlResourceURL + "-config"));
//    assertEquals(2, countOccurences(s, "get:\\pet:" + rootRamlResourceURL + "-config"));
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
  public void testSimpleGenerationV08() throws IOException {
    ScaffolderResourceLoader scaffolderResourceLoaderMock = Mockito.mock(ScaffolderResourceLoader.class);
    final String exchangeJsonResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:exchange.json";
    final String rootRamlResourceURL = "resource::com.mycompany:raml-api:1.0.0:raml:zip:simple.raml";


    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/exchange.json")).when(scaffolderResourceLoaderMock)
            .getResourceAsStream(exchangeJsonResourceURL);
    Mockito.doReturn(getInputStream("src/test/resources/scaffolder/simple.raml")).when(scaffolderResourceLoaderMock)
            .getResourceAsStream(rootRamlResourceURL);

    List<Dependency> dependencyList = new ArrayList<>();
    dependencyList.add(createDependency("com.mycompany", "raml-api", "1.0.0", "raml", "zip"));
    List<File> xmls = Arrays.asList();
    File muleXmlOut = folder.newFolder("mule-xml-out");

    new ScaffolderAPI().run(dependencyList, scaffolderResourceLoaderMock, muleXmlOut, null, DEFAULT_MULE_VERSION, DEFAULT_RUNTIME_EDITION);

    File muleXmlSimple = new File(muleXmlOut, "resource::com.mycompany:raml-api:1.0.0:raml:zip:simple.xml");

    assertTrue(muleXmlSimple.exists());
    String s = IOUtils.toString(new FileInputStream(muleXmlSimple));
    assertEquals(1, countOccurences(s, "http:listener-config name=\"resource::com.mycompany:raml-api:1.0.0:raml:zip:simple"));
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
//    assertEquals(2, countOccurences(s, "get:\\:" + rootRamlResourceURL + "-config"));
//    assertEquals(2, countOccurences(s, "get:\\pet:" + rootRamlResourceURL + "-config"));
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
