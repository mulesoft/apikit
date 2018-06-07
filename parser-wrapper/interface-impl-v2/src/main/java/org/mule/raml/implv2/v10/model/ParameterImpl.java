/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.metadata.api.model.MetadataType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.raml.v2.api.model.v10.system.types.AnnotableStringType;
import org.raml.v2.api.model.v10.system.types.MarkdownString;

import static org.mule.raml.implv2.v10.MetadataResolver.anyType;
import static org.mule.raml.implv2.v10.MetadataResolver.resolve;

public class ParameterImpl implements IParameter {

  private TypeDeclaration typeDeclaration;

  public ParameterImpl(TypeDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;
  }

  @Override
  public boolean validate(String value) {
    List<ValidationResult> results = typeDeclaration.validate(value);
    return results.isEmpty();
  }

  @Override
  public String message(String value) {
    List<ValidationResult> results = typeDeclaration.validate(value);
    return results.isEmpty() ? "OK" : results.get(0).getMessage();
  }

  @Override
  public boolean isRequired() {
    return typeDeclaration.required();
  }

  @Override
  public String getDefaultValue() {
    return typeDeclaration.defaultValue();
  }

  @Override
  public boolean isRepeat() {
    // only available in RAML 0.8
    return false;
  }

  @Override
  public boolean isArray() {
    return typeDeclaration instanceof ArrayTypeDeclaration;
  }

  @Override
  public String getDisplayName() {
    final AnnotableStringType type = typeDeclaration.displayName();
    return type == null ? null : type.value();
  }

  @Override
  public String getDescription() {
    final MarkdownString description = typeDeclaration.description();
    return description == null ? null : description.value();
  }

  @Override
  public String getExample() {
    if (typeDeclaration.example() == null) {
      return null;
    }
    return typeDeclaration.example().value();
  }

  @Override
  public Map<String, String> getExamples() {
    Map<String, String> map = new LinkedHashMap<>();
    final List<ExampleSpec> examples = typeDeclaration.examples();
    
    if (examples.isEmpty()) {
        final String example = getExample();
        if (example != null) {
            map.put(null, example);
        }
    }
    else {
        for (ExampleSpec example : examples) {
            map.put(example.name(), example.value());
        }
    }
    return map;
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataType getMetadata() {
    return resolve(typeDeclaration).orElse(anyType());
  }
}
