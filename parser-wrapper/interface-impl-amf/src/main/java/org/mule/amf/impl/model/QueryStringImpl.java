/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.exceptions.UnsupportedSchemaException;
import org.mule.raml.interfaces.model.IQueryString;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class QueryStringImpl implements IQueryString {

  private AnyShape schema;
  private boolean required;
  private Collection<String> scalarTypes;


  QueryStringImpl(final AnyShape anyShape, boolean required) {
    this.schema = anyShape;
    this.required = required;

    final List<ScalarType> typeIds = asList(ScalarType.values());

    this.scalarTypes = typeIds.stream().map(ScalarType::getName).collect(Collectors.toList());
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
  public String getDefaultValue() {
    return schema.defaultValueStr().option().orElse(null);
  }

  @Override
  public boolean isArray() {
    return schema instanceof ArrayShape;
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

  @Override
  public boolean isScalar() {
    return scalarTypes.contains(schema.id());
  }

  @Override
  public boolean isFacetArray(String facet) {
    if (schema instanceof NodeShape) {
      for (PropertyShape type : ((NodeShape) schema).properties()) {
        if (facet.equals(type.name().value()))
          return type.range() instanceof ArrayShape;
      }
    }
    return false;
  }

  @Override
  public Map<String, IParameter> facets() {
    HashMap<String, IParameter> result = new HashMap<>();

    if (schema instanceof NodeShape) {
      for (PropertyShape type : ((NodeShape) schema).properties())
        result.put(type.name().value(), new ParameterImpl(type));
    }
    return result;
  }
}
