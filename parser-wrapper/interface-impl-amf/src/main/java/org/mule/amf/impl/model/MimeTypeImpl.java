/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Payload;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.validate.ValidationReport;
import org.mule.amf.impl.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.raml.interfaces.parser.rule.IValidationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class MimeTypeImpl implements IMimeType {

  Payload payload;

  public MimeTypeImpl(final Payload payload) {
    this.payload = payload;
  }

  @Override
  public Object getCompiledSchema() {
    return null;
  }

  @Override
  public String getSchema() {
    final Shape schema = payload.schema();

    if (schema.getClass() == AnyShape.class)
      return null;

    if (schema instanceof AnyShape)
      return ((AnyShape) schema).toJsonSchema();

    return null;
  }

  @Override
  public Map<String, List<IParameter>> getFormParameters() {
    final String mediaType = payload.mediaType().value();

    if (mediaType.startsWith("multipart/") || mediaType.equals("application/x-www-form-urlencoded")) {
      final Shape schema = payload.schema();

      if (schema.getClass() == AnyShape.class)
        return emptyMap();

      if (!(schema instanceof NodeShape))
        throw new RuntimeException("Unexpected Shape " + schema.getClass());

      final NodeShape nodeShape = cast(schema);

      final Map<String, List<IParameter>> parameters = new LinkedHashMap<>();

      for (PropertyShape propertyShape : nodeShape.properties()) {
        parameters.put(propertyShape.name().value(), singletonList(new ParameterImpl(propertyShape)));
      }

      return parameters;
    }

    return emptyMap();
  }

  @Override
  public String getType() {
    return payload.mediaType().value();
  }

  @Override
  public String getExample() {
    final Shape schema = payload.schema();

    if (schema instanceof AnyShape) {
      final AnyShape anyShape = (AnyShape) schema;
      return anyShape.examples().stream().filter(example -> example.name().value() == null)
          .map(example -> example.value().value())
          .findFirst()
          .orElse(null);
    }

    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public List<IValidationResult> validate(String payload) {
    final Shape schema = this.payload.schema();

    if (schema instanceof AnyShape) {
      try {
        final ValidationReport validationReport = ((AnyShape) schema).validate(payload).get();
        if (validationReport.conforms())
          return emptyList();
        else
          return validationReport.results().stream().map(ValidationResultImpl::new).collect(toList());
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Unexpected Error validating payload");
      }
    }

    return null;
  }
}
