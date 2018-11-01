/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class RamlApiWrapper {

  private final Map<String, IResource> ramlResources = new HashMap<>();
  private final Map<String, IParameter> baseUriParameters;
  private final Map<String, String> consolidatedSchemas;
  private final Notifier notifier;

  public RamlApiWrapper(IRaml ramlApi, Notifier notifier) {
    collectResources(ramlApi.getResources(), ramlApi.getVersion());
    consolidatedSchemas = ramlApi.getConsolidatedSchemas();
    this.baseUriParameters = ramlApi.getBaseUriParameters();
    this.notifier = notifier;
  }

  private void collectResources(Map<String, IResource> resources, String version) {
    resources.values().forEach(resource -> {
      ramlResources.put(resource.getResolvedUri(version), resource);
      collectResources(resource.getResources(), version);
    });
  }

  public Optional<MetadataSource> getActionForFlow(RamlApiWrapper api, ApiCoordinate coordinate, String httpStatusVar,
                                                   String outboundHeadersVar) {
    return ofNullable(ramlResources.get(coordinate.getResource()))
        .map(resource -> resource.getAction(coordinate.getMethod()))
        .map(action -> new FlowMetadata(api, action, coordinate, baseUriParameters, httpStatusVar, outboundHeadersVar, notifier));
  }

  public Map<String, String> getConsolidatedSchemas() {
    return consolidatedSchemas;
  }
}


