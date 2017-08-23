/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.junit.Test;
import org.mule.module.apikit.metadata.model.RamlCoordinate;

import java.util.HashSet;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RamlCoordsSimpleFactoryTest {

  @Test
  public void twoConfigsTest() {
    final RamlCoordsSimpleFactory factory = new RamlCoordsSimpleFactory(set("config1", "config2"));

    Optional<RamlCoordinate> coord = factory.createFromFlowName("get:/persons:config1");
    assertTrue(coord.isPresent());
    assertEquals("get:/persons:config1", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertEquals("config1", coord.get().getConfigName());

    coord = factory.createFromFlowName("post:/offices:config2");
    assertTrue(coord.isPresent());
    assertEquals("post:/offices:config2", coord.get().getFlowName());
    assertEquals("post", coord.get().getMethod());
    assertEquals("/offices", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertEquals("config2", coord.get().getConfigName());

    coord = factory.createFromFlowName("post:/offices:application/json:config2");
    assertTrue(coord.isPresent());
    assertEquals("post:/offices:application/json:config2", coord.get().getFlowName());
    assertEquals("post", coord.get().getMethod());
    assertEquals("/offices", coord.get().getResource());
    assertEquals("application/json", coord.get().getMediaType());
    assertEquals("config2", coord.get().getConfigName());

    coord = factory.createFromFlowName("post:/offices");
    assertTrue(!coord.isPresent());

    coord = factory.createFromFlowName("post:/incomplete:application/json:config2:illegal");
    assertTrue(!coord.isPresent());

    coord = factory.createFromFlowName("post:/offices:application/json:unknown-config");
    assertTrue(!coord.isPresent());
  }

  @Test
  public void oneConfigTest() {
    final RamlCoordsSimpleFactory factory = new RamlCoordsSimpleFactory(set("config"));

    Optional<RamlCoordinate> coord = factory.createFromFlowName("get:/persons");
    assertTrue(coord.isPresent());
    assertEquals("get:/persons", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertNull(coord.get().getConfigName());

    coord = factory.createFromFlowName("get:/persons");
    assertTrue(coord.isPresent());
    assertEquals("get:/persons", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertNull(coord.get().getMediaType());
    assertNull(coord.get().getConfigName());

    coord = factory.createFromFlowName("get:/persons:application/json:config");
    assertTrue(coord.isPresent());
    assertEquals("get:/persons:application/json:config", coord.get().getFlowName());
    assertEquals("get", coord.get().getMethod());
    assertEquals("/persons", coord.get().getResource());
    assertEquals("application/json", coord.get().getMediaType());
    assertEquals("config", coord.get().getConfigName());

    coord = factory.createFromFlowName("get:/persons:application/json:unknownConfig");
    assertTrue(!coord.isPresent());
  }

  private HashSet<String> set(String... configs) {
    return new HashSet<>(asList(configs));
  }

}
