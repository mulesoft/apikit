/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.CoreMatchers.hasItems;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.uri.URIPattern;

public class RoutingTableTestCase {
  RamlHandler ramlHandler = new RamlHandler("org/mule/module/apikit/routing-table-sample.raml", true);

  public RoutingTableTestCase() throws IOException {
  }

  @Test
  public void testResourceFlattenedTree() throws IOException {

    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());


    Assert.assertThat(routingTable.keySet(), hasItems(new URIPattern("/single-resource"),
                                                      new URIPattern("/api"),
                                                      new URIPattern("/api/sub-resource"),
                                                      new URIPattern("/api/sub-resource-types")));
  }

  @Test
  public void getResourceByPattern() {
    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());

    Assert.assertNotNull(routingTable.getResource(new URIPattern("/single-resource")));
    Assert.assertNotNull(routingTable.getResource(new URIPattern("/api/sub-resource")));
  }

  @Test
  public void getResourceByString() {
    RoutingTable routingTable = new RoutingTable(ramlHandler.getApi());

    Assert.assertNotNull(routingTable.getResource("/single-resource"));
    Assert.assertNotNull(routingTable.getResource("/api/sub-resource"));
  }
}
