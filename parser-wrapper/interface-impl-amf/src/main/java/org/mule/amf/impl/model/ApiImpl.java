/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Server;
import amf.client.model.domain.WebApi;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public class ApiImpl implements IRaml {

  private WebApi webApi;
  private Map<String, Map<String, IResource>> resources = new HashMap<>();

  public ApiImpl(final WebApi webApi) {
    this.webApi = webApi;
    resources = buildResources(webApi.endPoints());
  }

  private Map<String, Map<String, IResource>> buildResources(final List<EndPoint> endPoints) {

    final Map<String, Map<String, IResource>> resources = new HashMap<>();

    endPoints.forEach(endPoint -> {

      final String value = endPoint.path().value();
      final int index = value.lastIndexOf("/");
      final String parent = value.substring(0, index);
      addToMap(resources, parent, endPoint);
    });
    return resources;

  }

  private void addToMap(final Map<String, Map<String, IResource>> resources, final String parent, final EndPoint endPoint) {
    final Map<String, IResource> parentMap = resources.computeIfAbsent(parent, k -> new HashMap<>());
    parentMap.put(parent, new ResourceImpl(endPoint));
  }

  @Override
  public IResource getResource(String path) {
    return getResources().get(path);
  }

  @Override
  public Map<String, String> getConsolidatedSchemas() {
    return null;
  }

  @Override
  public Map<String, Object> getCompiledSchemas() {
    return null;
  }

  @Override
  public String getBaseUri() {
    return getServer().map(server -> server.url().value()).orElse(null);
  }

  private Optional<Server> getServer() {
    return webApi.servers().stream().findFirst();
  }


  @Override
  public Map<String, IResource> getResources() {
    return resources.get("/");
  }

  @Override
  public String getVersion() {
    return webApi.version().value();
  }

  @Override
  public Map<String, IParameter> getBaseUriParameters() {
    return getServer().<Map<String, IParameter>>map(server -> server.variables().stream()
        .collect(toMap(p -> p.name().value(), ParameterImpl::new)))
        .orElseGet(Collections::emptyMap);
  }

  @Override
  public List<Map<String, ISecurityScheme>> getSecuritySchemes() {
    return null;
  }

  @Override
  public List<Map<String, ITemplate>> getTraits() {
    return null;
  }

  @Override
  public String getUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Map<String, String>> getSchemas() {
    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public void cleanBaseUriParameters() {

  }

  @Override
  public void injectTrait(String name) {

  }

  @Override
  public void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme) {

  }

  public Map<String, Map<String, IResource>> getResourcesTree() {
    return resources;
  }
}
