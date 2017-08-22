/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.mule.module.apikit.metadata.interfaces.MetadataSource;
import org.mule.module.apikit.metadata.FlowMetadata;
import org.mule.module.apikit.metadata.model.RamlCoordinate;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;

import java.util.HashMap;
import java.util.Map;

public class RamlApiWrapper {

  private Map<String, IResource> ramlResources = new HashMap<>();


  public RamlApiWrapper(IRaml ramlApi) {
    collectResourcesRecursively(ramlApi.getResources());
  }

  private void collectResourcesRecursively(Map<String, IResource> resources) {
    for (IResource resource : resources.values()) {
      ramlResources.put(resource.getUri(), resource);
      collectResourcesRecursively(resource.getResources());
    }
  }

  public MetadataSource getActionForCoordinate(RamlCoordinate coordinate) {
    IResource resource = ramlResources.get(coordinate.getResource());
    IAction action = resource.getAction(coordinate.getMethod());
    return new FlowMetadata(action, coordinate);
  }
}


