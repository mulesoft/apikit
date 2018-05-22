/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.Payload;
import java.util.List;
import java.util.Map;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.raml.interfaces.parser.rule.IValidationResult;

public class MimeTypeImpl implements IMimeType {

  public MimeTypeImpl(final Payload payload) {}

  @Override
  public Object getCompiledSchema() {
    return null;
  }

  @Override
  public String getSchema() {
    return null;
  }

  @Override
  public Map<String, List<IParameter>> getFormParameters() {
    return null;
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public String getExample() {
    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public List<IValidationResult> validate(String payload) {
    return null;
  }
}
