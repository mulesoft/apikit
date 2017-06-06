/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mule.module.apikit.uri.URIPattern;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;

public class RoutingTable {

  protected Map<URIPattern, IResource> routingTable = new HashMap<>();

  public RoutingTable(IRaml api) {

    buildRoutingTable(api.getResources(), api.getVersion());
  }

  private void buildRoutingTable(Map<String, IResource> resources, String version) {

    for (IResource resource : resources.values()) {

      String parentUri = resource.getParentUri();

      if (parentUri.contains("{version}")) {
        resource.setParentUri(parentUri.replaceAll("\\{version}", version));
      }

      String uri = resource.getUri();

      routingTable.put(new URIPattern(uri), resource);

      if (resource.getResources() != null) {
        buildRoutingTable(resource.getResources(), version);
      }

    }
  }

  public IResource getResource(String uri) {
    return routingTable.get(new URIPattern(uri));
  }

  public IResource getResource(URIPattern uriPattern) {
    return routingTable.get(uriPattern);
  }

  public Set<URIPattern> keySet() {
    return routingTable.keySet();
  }
}
