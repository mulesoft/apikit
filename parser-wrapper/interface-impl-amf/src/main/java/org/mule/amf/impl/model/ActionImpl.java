/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Request;
import amf.client.model.domain.Response;
import amf.client.model.domain.Shape;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IQueryString;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.ISecurityReference;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public class ActionImpl implements IAction {

  private final ResourceImpl resource;
  private final Operation operation;
  private Map<String, IMimeType> bodies;
  private Map<String, IResponse> responses;
  private Map<String, IParameter> queryParameters;
  private Map<String, IParameter> headers;
  private Map<String, IParameter> resolvedUriParameters;

  public ActionImpl(final ResourceImpl resource, final Operation operation) {
    this.resource = resource;
    this.operation = operation;
  }

  @Override
  public IActionType getType() {
    return IActionType.valueOf(operation.method().value().toUpperCase());
  }

  @Override
  public boolean hasBody() {
    return !getBody().isEmpty();
  }

  @Override
  public Map<String, IResponse> getResponses() {
    if (responses == null) {
      responses = loadResponses(operation);
    }
    return responses;
  }

  private static Map<String, IResponse> loadResponses(final Operation operation) {
    Map<String, IResponse> result = new LinkedHashMap<>();
    for (Response response : operation.responses()) {
      result.put(response.statusCode().value(), new ResponseImpl(response));
    }
    return result;
  }

  @Override
  public IResource getResource() {
    return resource;
  }

  @Override
  public Map<String, IMimeType> getBody() {
    if (bodies == null) {
      bodies = loadBodies(operation);
    }

    return bodies;
  }

  private static Map<String, IMimeType> loadBodies(final Operation operation) {
    final Request request = operation.request();
    if (request == null)
      return emptyMap();

    final Map<String, IMimeType> result = new LinkedHashMap<>();

    request.payloads().stream()
        .filter(payload -> payload.mediaType().nonNull())
        .forEach(payload -> result.put(payload.mediaType().value(), new MimeTypeImpl(payload)));

    return result;
  }

  @Override
  public Map<String, IParameter> getQueryParameters() {
    if (queryParameters == null) {
      queryParameters = loadQueryParameters(operation);
    }
    return queryParameters;
  }

  private static Map<String, IParameter> loadQueryParameters(final Operation operation) {
    final Request request = operation.request();
    if (request == null)
      return emptyMap();

    final Map<String, IParameter> result = new HashMap<>();
    request.queryParameters().forEach(parameter -> {
      result.put(parameter.name().value(), new ParameterImpl(parameter));
    });
    return result;
  }

  @Override
  public Map<String, List<IParameter>> getBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, IParameter> getResolvedUriParameters() {
    if (resolvedUriParameters == null) {
      resolvedUriParameters = loadResolvedUriParameters(resource, operation);
    }

    return resolvedUriParameters;
  }

  private static Map<String, IParameter> loadResolvedUriParameters(final IResource resource, Operation operation) {
    final Map<String, IParameter> operationUriParams;
    if (operation.request() != null) {
      operationUriParams = operation.request().uriParameters().stream()
          .collect(toMap(p -> p.name().value(), ParameterImpl::new));
    } else {
      operationUriParams = new HashMap<>();
    }

    final Map<String, IParameter> uriParameters = resource.getResolvedUriParameters();
    uriParameters.forEach(operationUriParams::putIfAbsent);

    return operationUriParams;
  }

  @Override
  public Map<String, IParameter> getHeaders() {
    if (headers == null) {
      headers = loadHeaders(operation);
    }
    return headers;
  }

  private Map<String, IParameter> loadHeaders(final Operation operation) {
    final Request request = operation.request();
    if (request == null)
      return emptyMap();

    final Map<String, IParameter> result = new HashMap<>();
    request.headers().forEach(parameter -> {
      result.put(parameter.name().value(), new ParameterImpl(parameter));
    });
    return result;
  }

  @Override
  public List<ISecurityReference> getSecuredBy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getIs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanBaseUriParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeaders(Map<String, IParameter> headers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setQueryParameters(Map<String, IParameter> queryParameters) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBody(Map<String, IMimeType> body) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addResponse(String key, IResponse response) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSecurityReference(String securityReferenceName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addIs(String is) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IQueryString queryString() {
    final Request request = operation.request();

    if (request == null)
      return null;

    final Shape shape = request.queryString();
    return shape == null ? null : new QueryStringImpl((AnyShape) shape, request.required().value());
  }
}
