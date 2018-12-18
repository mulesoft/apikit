/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import org.junit.Test;
import org.mule.raml.interfaces.loader.ClassPathResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InterfaceV08TestCase {

  @Test
  public void references() {
    String path = new ClassPathResourceLoader().getResource("org/mule/raml/implv1/api.raml").getPath();
    IRaml raml = new ParserWrapperV1(path).build();

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", ref, startsWith("file:/")));
    assertEquals(8, allReferences.size());
  }
}
