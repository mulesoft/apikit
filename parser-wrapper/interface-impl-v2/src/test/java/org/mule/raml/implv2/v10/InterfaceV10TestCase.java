/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InterfaceV10TestCase {

  @Test
  public void check() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    IRaml raml = ParserV2Utils.build(resourceLoader, "org/mule/raml/implv2/v10/full-1.0.raml");
    assertThat(raml.getVersion(), is("1.0"));
    assertThat(raml.getSchemas().get(0).size(), is(2));
    assertThat(raml.getSchemas().get(0).get("User"),
               is("{\"$ref\":\"#/definitions/User\",\"definitions\":{\"User\":{\"type\":\"object\",\"properties\":{\"firstname\":{\"type\":\"string\"},\"lastname\":{\"type\":\"string\"},\"age\":{\"type\":\"number\"}},\"required\":[\"firstname\",\"lastname\",\"age\"]}},\"$schema\":\"http://json-schema.org/draft-04/schema#\"}"));
    assertThat(raml.getSchemas().get(0).get("UserJson"), CoreMatchers.containsString("firstname"));
  }

  @Test
  public void references() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    IRaml raml = ParserV2Utils.build(resourceLoader, "org/mule/raml/implv2/v10/references/api.raml");

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", ref, startsWith("file:/")));
    assertEquals(4, allReferences.size());
  }
}
