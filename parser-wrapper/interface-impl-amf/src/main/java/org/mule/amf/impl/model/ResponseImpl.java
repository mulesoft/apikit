/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.Response;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ResponseImpl implements IResponse {

  Response response;

  public ResponseImpl(Response response) {
    this.response = response;
  }

  @Override
  public Map<String, IMimeType> getBody() {
    return response.payloads().stream()
        .filter(p -> p.mediaType().nonNull())
        .collect(toMap(p -> p.mediaType().value(), MimeTypeImpl::new));
  }

  @Override
  public boolean hasBody() {
    return !response.payloads().isEmpty() && response.payloads().stream().anyMatch(p -> p.mediaType().nonNull());
  }

  @Override
  public Map<String, IParameter> getHeaders() {
    return null;
  }

  @Override
  public void setBody(Map<String, IMimeType> body) {

  }

  @Override
  public void setHeaders(Map<String, IParameter> headers) {

  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public Map<String, String> getExamples() {
    final Map<String, String> result = IResponse.super.getExamples();

    response.examples().forEach(example -> result.put(example.mediaType().value(), example.value().value()));

    return result;
  }
}
