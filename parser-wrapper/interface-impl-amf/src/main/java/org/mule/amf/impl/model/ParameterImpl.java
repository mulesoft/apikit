/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.Shape;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import org.mule.amv.impl.extension.UnsupportedSchemaException;
import org.mule.metadata.api.model.MetadataType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.util.stream.Collectors.toMap;

public class ParameterImpl implements IParameter {

  private Parameter parameter;
  private AnyShape schema;

  public ParameterImpl(Parameter parameter) {
    this.parameter = parameter;
  }

  @Override
  public boolean validate(String value) {
    return validatePayload(value).conforms();
  }

  private ValidationReport validatePayload(String value) {
    final AnyShape schema = getSchema();
    try {
      return schema.validate(value).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error validating request", e);
    }
  }

  private AnyShape getSchema() {
    if (schema == null) {
      final Shape shape = parameter.schema();

      if (schema instanceof AnyShape)
        schema = cast(shape);

      throw new UnsupportedSchemaException();
    }

    return schema;
  }

  @Override
  public String message(String value) {
    final ValidationReport validationReport = validatePayload(value);
    if (validationReport.conforms())
      return "OK";
    else {
      return validationReport.results().stream()
          .findFirst()
          .map(ValidationResult::message)
          .orElse("Error");
    }
  }

  @Override
  public boolean isRequired() {
    return parameter.required().value();
  }

  @Override
  public String getDefaultValue() {
    return schema.defaultValueStr().value();
  }

  @Override
  public boolean isRepeat() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isArray() {
    return getSchema() instanceof ArrayShape;
  }

  @Override
  public String getDisplayName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getExample() {
    return getExamples().values().stream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public Map<String, String> getExamples() {
    return getSchema().examples().stream()
        .collect(toMap(e -> e.name().value(), e -> e.value().value()));
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataType getMetadata() {
    throw new UnsupportedOperationException();
  }
}
