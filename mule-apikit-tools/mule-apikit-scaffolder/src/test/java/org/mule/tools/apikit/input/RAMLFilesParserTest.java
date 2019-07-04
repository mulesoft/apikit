/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.maven.plugin.logging.Log;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;
import org.mule.tools.apikit.output.GenerationModel;

public class RAMLFilesParserTest {

  @Test
  public void testCreation() {

    final URL resourceUrl =
        RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/simple.raml");

    assertNotNull(resourceUrl);

    final List<String> list = urlToList(resourceUrl);

    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(mockLog(), list, new APIFactory());

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
    assertEquals("0.0.0.0", triplet.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("simple-httpListenerConfig", triplet.getApi().getHttpListenerConfig().getName());
    ResourceActionMimeTypeTriplet triplet2 = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/pet".equals(triplet.getUri()) && "GET".equals(triplet.getVerb())
            && "/api".equals(triplet.getApi().getPath());
      }
    });
    assertEquals("0.0.0.0", triplet2.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet2.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet2.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("simple-httpListenerConfig", triplet2.getApi().getHttpListenerConfig().getName());

  }

  @Test
  @Ignore
  public void apiWithWarningsShouldBeValid() {

    final URL resourceUrl =
        RAMLFilesParserTest.class.getClassLoader().getResource("scaffolder/apiWithWarnings.raml");

    assertNotNull(resourceUrl);

    final List<String> list = urlToList(resourceUrl);

    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(mockLog(), list, new APIFactory());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();
    assertNotNull(entries);
    assertEquals(1, entries.size());
  }


  @Test
  public void oasCreation() {

    final URL url =
        RAMLFilesParserTest.class.getClassLoader()
            .getResource("oas/OpenAPI-Specification/examples/v2.0/json/src/main/resources/api/petstore.json");

    System.out.println("RAMLFilesParserTest.oasCreation " + url);

    final List<String> streams = urlToList(url);

    RAMLFilesParser ramlFilesParser = RAMLFilesParser.create(mockLog(), streams, new APIFactory());

    Map<ResourceActionMimeTypeTriplet, GenerationModel> entries = ramlFilesParser.getEntries();

    assertNotNull(entries);
    assertEquals(3, entries.size());
    Set<ResourceActionMimeTypeTriplet> ramlEntries = entries.keySet();
    ResourceActionMimeTypeTriplet triplet = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/pets".equals(triplet.getUri()) && "GET".equals(triplet.getVerb())
            && "/api".equals(triplet.getApi().getPath());
      }
    });
    assertEquals("0.0.0.0", triplet.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("petstore-httpListenerConfig", triplet.getApi().getHttpListenerConfig().getName());
    ResourceActionMimeTypeTriplet triplet2 = (ResourceActionMimeTypeTriplet) CollectionUtils.find(ramlEntries, new Predicate() {

      @Override
      public boolean evaluate(Object property) {
        ResourceActionMimeTypeTriplet triplet = ((ResourceActionMimeTypeTriplet) property);
        return "/api/pets".equals(triplet.getUri()) && "GET".equals(triplet.getVerb())
            && "/api".equals(triplet.getApi().getPath());
      }
    });
    assertEquals("0.0.0.0", triplet2.getApi().getHttpListenerConfig().getHost());
    assertEquals("8081", triplet2.getApi().getHttpListenerConfig().getPort());
    assertEquals("/", triplet2.getApi().getHttpListenerConfig().getBasePath());
    assertEquals("petstore-httpListenerConfig", triplet2.getApi().getHttpListenerConfig().getName());


  }


  private static List<String> urlToList(final URL url) {
    final List<String> list = new ArrayList<>();
    list.add(url.getFile());
    return list;
  }


  private Stubber getStubber(String prefix) {
    return doAnswer((Answer<Void>) invocation -> {
      Object[] args = invocation.getArguments();
      System.out.println(prefix + args[0].toString());
      return null;
    });
  }

  private Log mockLog() {

    Log log = mock(Log.class);
    getStubber("[INFO] ").when(log).info(anyString());
    getStubber("[WARNING] ").when(log).warn(anyString());
    getStubber("[ERROR] ").when(log).error(anyString());

    return log;
  }
}
