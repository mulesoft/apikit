/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.EndPoint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.util.stream.Collectors.toMap;
import static org.mule.raml.interfaces.ParserUtils.resolveVersion;

public class ResourceImpl implements IResource {

  private AmfImpl amf;
  private EndPoint endPoint;
  private Map<IActionType, IAction> actions;
  private Map<String, IParameter> resolvedUriParameters;

  ResourceImpl(final AmfImpl amf, final EndPoint endPoint) {
    this.amf = amf;
    this.endPoint = endPoint;
  }

  @Override
  public String getRelativeUri() {
    return endPoint.relativePath();
  }

  @Override
  public String getUri() {
    return endPoint.path().value();
  }

  @Override
  public String getResolvedUri(final String version) {
    return resolveVersion(getUri(), version);
  }

  @Override
  public String getParentUri() {
    return getUri().substring(0, getUri().length() - getRelativeUri().length());
  }

  @Override
  public IAction getAction(final String name) {
    return getActions().get(getActionKey(name));
  }

  @Override
  public Map<IActionType, IAction> getActions() {
    if (actions == null)
      actions = loadActions(endPoint);

    return actions;
  }

  private Map<IActionType, IAction> loadActions(final EndPoint endPoint) {
    final Map<IActionType, IAction> map = new LinkedHashMap<>();
    endPoint.operations()
        .forEach(operation -> map.put(getActionKey(operation.method().value()), new ActionImpl(this, operation)));
    return map;
  }

  private static IActionType getActionKey(final String method) {
    return IActionType.valueOf(method.toUpperCase());
  }

  @Override
  public Map<String, IResource> getResources() {
    return amf.getResources(this);
  }

  @Override
  public String getDisplayName() {
    return getUri();
  }

  @Override
  public Map<String, IParameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(endPoint);
    }

    return resolvedUriParameters;
  }

  private static Map<String, IParameter> loadResolvedUriParameters(final EndPoint resource) {
    return resource.parameters().stream()
        .filter(p -> !"version".equals(p.name().value())) // version is an special uri param so it is ignored
        .collect(toMap(p -> p.name().value(), ParameterImpl::new));
  }

  @Override
  public void setParentUri(String parentUri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, List<IParameter>> getBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return getUri();
  }

}
