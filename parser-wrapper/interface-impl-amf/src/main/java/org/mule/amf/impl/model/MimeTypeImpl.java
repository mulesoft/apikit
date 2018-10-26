/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.model;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.Example;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Payload;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.client.validate.ValidationReport;
import amf.client.validation.PayloadValidator;
import org.mule.amf.impl.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.raml.interfaces.parser.rule.IValidationResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mule.amf.impl.model.MediaType.APPLICATION_XML;
import static org.mule.amf.impl.model.MediaType.getMimeTypeForValue;

public class MimeTypeImpl implements IMimeType {

  private final Payload payload;
  private final Shape shape;
  private final PayloadValidator validator;

  public MimeTypeImpl(final Payload payload) {
    this.payload = payload;
    this.shape = payload.schema();
    this.validator = shape instanceof AnyShape ? ((AnyShape) shape).payloadValidator() : null;
  }

  @Override
  public Object getCompiledSchema() {
    return null;
  }

  @Override
  public String getSchema() {
    if (shape.getClass() == AnyShape.class)
      return null;

    if (shape instanceof AnyShape)
      return ((AnyShape) shape).toJsonSchema();

    return null;
  }

  @Override
  public Map<String, List<IParameter>> getFormParameters() {
    final String mediaType = payload.mediaType().value();

    if (mediaType.startsWith("multipart/") || mediaType.equals("application/x-www-form-urlencoded")) {
      if (shape.getClass() == AnyShape.class)
        return emptyMap();

      if (!(shape instanceof NodeShape))
        throw new RuntimeException("Unexpected Shape " + shape.getClass());

      final NodeShape nodeShape = cast(shape);

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
    if (shape instanceof UnionShape) {
      final UnionShape unionShape = (UnionShape) shape;
      for (Shape shape : unionShape.anyOf()) {
        if (shape instanceof AnyShape) {
          final String example = getExampleFromAnyShape((AnyShape) shape);
          if (example != null)
            return example;
        }
      }
    }

    if (shape instanceof AnyShape) {
      return getExampleFromAnyShape((AnyShape) shape);
    }

    return null;
  }

  private String getExampleFromAnyShape(AnyShape anyShape) {
    final Optional<Example> trackedExample = anyShape.trackedExample(payload.id());

    if (trackedExample.isPresent()) {
      final Example example = trackedExample.get();
      if (example.value().nonNull())
        return example.value().value();
    }

    return anyShape.examples().stream().filter(example -> example.value().value() != null)
        .map(example -> example.value().value())
        .findFirst()
        .orElse(null);
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public List<IValidationResult> validate(String payload) {
    final String mimeType = getMimeTypeForValue(payload);

    if (APPLICATION_XML.equals(mimeType))
      return validateXml(payload);
    else
      return validatePayload(payload, mimeType);
  }

  private List<IValidationResult> validatePayload(String payload, String mimeType) {
    if (validator != null) {
      return mapToValidationResult(validator.reportValidation(mimeType, payload));
    }
    return null;
  }

  private List<IValidationResult> validateXml(String payload) {
    try {
      final ValidationReport validationReport = ((AnyShape) shape).validate(payload).get();
      return mapToValidationResult(validationReport);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unexpected Error validating payload");
    }
  }

  private static List<IValidationResult> mapToValidationResult(ValidationReport validationReport) {
    if (validationReport.conforms())
      return emptyList();
    else
      return validationReport.results().stream().map(ValidationResultImpl::new).collect(toList());
  }
}
