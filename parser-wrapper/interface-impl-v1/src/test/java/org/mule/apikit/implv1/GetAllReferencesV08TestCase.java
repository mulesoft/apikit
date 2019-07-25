/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv1;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mule.raml.implv1.model.RamlImplV1;
import org.raml.model.Raml;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetAllReferencesV08TestCase {

  @Test
  public void references() {
    final String relativePath = "org/mule/apikit/implv1/api.raml";
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
    Raml api = builder.build(relativePath);
    RamlImplV1 raml = new RamlImplV1(api,relativePath,resourceLoader);

    List<String> allReferences = raml.getAllReferences();
    assertEquals(9, allReferences.size());

    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/versioned.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/base.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/collection.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/../examples/generic_error.xml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/schemas/atom.xsd"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/resourceTypes/emailed.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_2_0.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/securitySchemes/oauth_1_0.raml"), CoreMatchers.is(true));
    assertThat(anyMatch(allReferences, "org/mule/apikit/implv1/traits/override-checked.raml"), CoreMatchers.is(true));
  }

  private boolean anyMatch(List<String> allReferences, String s) {
    for(String reference: allReferences){
      if(reference.endsWith(s)){
        return true;
      }
    }
    return false;
  }

}
