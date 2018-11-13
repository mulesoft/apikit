/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.Test;
import org.mule.extension.http.api.HttpRequestAttributes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.mule.module.apikit.metadata.internal.model.HttpRequestAttributesFields;

import static com.google.common.collect.Sets.difference;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * These tests try to detect when a class exposed in APIKit metadata like HttpRequestAttributes
 * adds/removes a field.
 * If a test case of this test suite fails when should update the metadata information exposed.
 */
public class MetadataDetectionChangesTest {

  @Test
  public void httpRequestAttributesChanges() {
    final Set<String> currentFields = stream(HttpRequestAttributesFields.values())
        .map(HttpRequestAttributesFields::getName)
        .collect(toSet());


    final Set<String> expectedFields = getAllFields(new ArrayList<>(), HttpRequestAttributes.class)
        .filter(f -> !isStatic(f.getModifiers()))
        .filter(f -> isPrivate(f.getModifiers()) || isProtected(f.getModifiers()))
        .map(Field::getName).collect(toSet());

    assertThat(difference(expectedFields, currentFields).size(), is(0));
    assertThat(difference(currentFields, expectedFields).size(), is(0));
  }

  private static Stream<Field> getAllFields(List<Field> fields, Class<?> type) {
    fields.addAll(asList(type.getDeclaredFields()));

    if (type.getSuperclass() != null) {
      getAllFields(fields, type.getSuperclass());
    }

    return fields.stream();
  }



}
