/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import org.mule.raml.interfaces.model.api.ApiRef;

public class ParserWrapperAmfTestCase {

  @Test
  public void testGetAllReferences() throws Exception {
    String pathAsUri =
        requireNonNull(getClass().getClassLoader().getResource("org/mule/amf/impl/ref-json-schema/input.raml")).toString();

    List<String> references = ParserWrapperAmf.create(ApiRef.create(pathAsUri), true).build().getAllReferences();

    assertThat(references.size(), is(2));
    assertThat(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/ref-json-schema/car-schema.json")), is(true));
    assertThat(references.stream().anyMatch(ref -> ref.endsWith("org/mule/amf/impl/ref-json-schema/car-properties-schema.json")),
               is(true));
  }

}
