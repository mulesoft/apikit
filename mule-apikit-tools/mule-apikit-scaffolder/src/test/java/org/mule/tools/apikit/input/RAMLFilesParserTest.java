/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import junit.framework.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class RAMLFilesParserTest {

  @Test
  public void testCreation() {
    Log log = mock(Log.class);
    getStubber("[INFO] ").when(log).info(anyString());
    getStubber("[WARNING] ").when(log).warn(anyString());
    getStubber("[ERROR] ").when(log).error(anyString());

    final URL resourceUrl =
        RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/simple.raml");

    assertNotNull(resourceUrl);

    InputStream resourceAsStream;
    try {
      resourceAsStream = resourceUrl.openStream();
    } catch (IOException e) {
      resourceAsStream = null;
    }

    HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
    streams.put(new File(resourceUrl.getFile()), resourceAsStream);

    RAMLFilesParser ramlFilesParser = new RAMLFilesParser(log, streams, new APIFactory());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
    assertNotNull(entries);
    assertEquals(5, entries.size());
    Set<ResourceActionMimeTypeTriplet> ramlEntries = entries.keySet();
    ResourceActionMimeTypeTriplet triplet = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/".equals(triplet.getUri()) && "GET".equals(triplet.getVerb()) && "/api".equals(triplet.getApi().getPath());
      }
    });
    Assert.assertEquals("0.0.0.0", triplet.getApi().getHttpListenerConfig().getHost());
    Assert.assertEquals("8081", triplet.getApi().getHttpListenerConfig().getPort());
    Assert.assertEquals("/", triplet.getApi().getHttpListenerConfig().getBasePath());
    Assert.assertEquals("simple-httpListenerConfig", triplet.getApi().getHttpListenerConfig().getName());
    ResourceActionMimeTypeTriplet triplet2 = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/pet".equals(triplet.getUri()) && "GET".equals(triplet.getVerb())
            && "/api".equals(triplet.getApi().getPath());
      }
    });
    Assert.assertEquals("0.0.0.0", triplet2.getApi().getHttpListenerConfig().getHost());
    Assert.assertEquals("8081", triplet2.getApi().getHttpListenerConfig().getPort());
    Assert.assertEquals("/", triplet2.getApi().getHttpListenerConfig().getBasePath());
    Assert.assertEquals("simple-httpListenerConfig", triplet2.getApi().getHttpListenerConfig().getName());

  }

  @Test
  public void apiWithWarningsShouldBeValid() {
    Log log = mock(Log.class);
    getStubber("[INFO] ").when(log).info(anyString());
    getStubber("[WARNING] ").when(log).warn(anyString());
    getStubber("[ERROR] ").when(log).error(anyString());


    final URL resourceUrl =
        RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/apiWithWarnings.raml");

    assertNotNull(resourceUrl);

    InputStream resourceAsStream;
    try {
      resourceAsStream = resourceUrl.openStream();
    } catch (IOException e) {
      resourceAsStream = null;
    }

    final HashMap<File, InputStream> streams = new HashMap<File, InputStream>();
    streams.put(new File(resourceUrl.getFile()), resourceAsStream);

    RAMLFilesParser ramlFilesParser = new RAMLFilesParser(log, streams, new APIFactory());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());
  }

  private Stubber getStubber(String prefix) {
    return doAnswer((Answer<Void>) invocation -> {
      Object[] args = invocation.getArguments();
      System.out.println(prefix + args[0].toString());
      return null;
    });
  }
}
