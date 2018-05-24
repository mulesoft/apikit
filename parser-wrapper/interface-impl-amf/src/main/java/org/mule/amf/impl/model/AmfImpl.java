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

public class AmfImpl implements IRaml {

  private WebApi webApi;
  private Map<String, Map<String, IResource>> resources = new HashMap<>();

  public AmfImpl(final WebApi webApi) {
    this.webApi = webApi;
    resources = buildResources(webApi.endPoints());

    //System.out.println("------------- Resources -------------");
    //System.out.println(dump("",  getResources(), ""));
    //System.out.println("-------------------------------------");
  }

  private static String dump(final String indent, Map<String, IResource> resources, String out) {

    if (resources.isEmpty())
      return out;

    for (Map.Entry<String, IResource> entry : resources.entrySet()) {

      final IResource value = entry.getValue();
      final String resource = "[" + entry.getKey() + "] -> " + value.getUri();
      out += indent + resource + "\n";
      if (value.getResources().isEmpty())
        continue;

      out = dump(indent + "  ", value.getResources(), out);
    }
    return out;
  }

  private Map<String, Map<String, IResource>> buildResources(final List<EndPoint> endPoints) {

    final Map<String, Map<String, IResource>> resources = new HashMap<>();
    endPoints.forEach(endPoint -> addToMap(resources, endPoint));
    return resources;
  }

  private void addToMap(final Map<String, Map<String, IResource>> resources, final EndPoint endPoint) {
    final String parentKey = parentKey(endPoint);
    final Map<String, IResource> parentMap = resources.computeIfAbsent(parentKey, k -> new HashMap<>());
    final String childKey = endPoint.relativePath();
    parentMap.put(childKey, new ResourceImpl(this, endPoint));
  }

  static String parentKey(final EndPoint endPoint) {
    final String path = endPoint.path().value();
    final int index = path.lastIndexOf("/");
    return index == 0 ? "/" : path.substring(0, index);
  }

  @Override
  public IResource getResource(final String path) {
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
    return resources.containsKey("/") ? resources.get("/") : emptyMap();
  }

  Map<String, IResource> getResources(final ResourceImpl resource) {
    final String key = resource.getUri();
    return resources.containsKey(key) ? resources.get(key) : emptyMap();
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
}
