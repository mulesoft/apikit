/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.Response;
import java.util.Map;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;

public class ResponseImpl implements IResponse {

  public ResponseImpl(Response response) {}

  @Override
  public Map<String, IMimeType> getBody() {
    return null;
  }

  @Override
  public boolean hasBody() {
    return false;
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
}
