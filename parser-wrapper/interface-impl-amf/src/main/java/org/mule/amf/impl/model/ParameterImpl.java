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
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.validate.ValidationReport;
import amf.client.validate.ValidationResult;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.metadata.api.model.MetadataType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.util.stream.Collectors.toMap;

class ParameterImpl implements IParameter {

  private AnyShape schema;
  private boolean required;

  ParameterImpl(final Parameter parameter) {
    this.schema = getSchema(parameter);
    this.required = parameter.required().value();
  }

  ParameterImpl(final PropertyShape property) {
    this.schema = castToAnyShape(property.range());
    this.required = property.minCount().value() > 0;
  }

  @Override
  public boolean validate(final String value) {
    return validatePayload(value).conforms();
  }

  private ValidationReport validatePayload(final String value) {
    try {
      return schema.validate(value).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected error validating request", e);
    }
  }

  private static AnyShape getSchema(final Parameter parameter) {
    final Shape shape = parameter.schema();
    return castToAnyShape(shape);
  }

  private static AnyShape castToAnyShape(Shape shape) {
    if (shape instanceof AnyShape)
      return (AnyShape) shape;
    throw new UnsupportedSchemaException();
  }

  @Override
  public String message(final String value) {
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
    return required;
  }

  @Override
  public String getDefaultValue() {
    return schema.defaultValueStr().value();
  }

  @Override
  public boolean isRepeat() {
    return schema instanceof ArrayShape;
  }

  @Override
  public boolean isArray() {
    return schema instanceof ArrayShape;
  }

  @Override
  public String getDisplayName() {
    return schema.displayName().value();
  }

  @Override
  public String getDescription() {
    return schema.description().value();
  }

  @Override
  public String getExample() {
    return getExamples().values().stream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public Map<String, String> getExamples() {
    return schema.examples().stream()
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
