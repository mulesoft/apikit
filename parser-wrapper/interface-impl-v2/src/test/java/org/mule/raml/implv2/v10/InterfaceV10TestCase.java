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

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InterfaceV10TestCase {

  private static final DefaultResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();

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
    final String relativePath = "org/mule/raml/implv2/v10/references/api.raml";
    final String pathAsUri = requireNonNull(getClass().getClassLoader().getResource(relativePath)).toString();
    final String absoulutPath = pathAsUri.substring(5);
    final String pathAsRemoteUrl =
        "https://raw.githubusercontent.com/mulesoft/apikit/M4-1.2.x/parser-wrapper/interface-impl-v2/src/test/resources/org/mule/raml/implv2/v10/references/api.raml";

    final List<String> paths = Arrays.asList(relativePath, pathAsUri, absoulutPath, pathAsRemoteUrl);
    paths.forEach(p -> checkReferences(p, DEFAULT_RESOURCE_LOADER));
  }

  private void checkReferences(String path, ResourceLoader resourceLoader) {
    System.out.println("Processing file = " + path);
    IRaml raml = ParserV2Utils.build(resourceLoader, path);

    List<String> allReferences = raml.getAllReferences();
    allReferences.forEach(ref -> assertThat("Invalid URI", URI.create(ref).toString(), is(ref)));
    assertEquals(6, allReferences.size());

    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/address.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/company-example.json", resourceLoader)),
               is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/partner.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/data-type.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/library.raml", resourceLoader)), is(true));
    assertThat(allReferences.stream()
        .anyMatch(r -> endWithAndExists(r, "org/mule/raml/implv2/v10/references/company.raml", resourceLoader)), is(true));
  }

  private boolean endWithAndExists(String reference, String goldenFile, ResourceLoader resourceLoader) {
    return reference.endsWith(goldenFile) && resourceLoader.fetchResource(reference) != null;
  }
}
