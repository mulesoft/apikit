/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import static org.mule.raml.implv2.v10.model.RamlImpl10V2.getTypeAsString;

import org.mule.raml.implv2.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

public class MimeTypeImpl implements IMimeType {

  private TypeDeclaration typeDeclaration;

  public MimeTypeImpl(TypeDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;
  }

  @Override
  public String getType() {
    return typeDeclaration.name();
  }

  @Override
  public String getExample() {
    ExampleSpec example = typeDeclaration.example();
    if (example != null && example.value() != null) {
      return example.value();
    }
    List<ExampleSpec> examples = typeDeclaration.examples();
    if (examples != null && !examples.isEmpty()) {
      if (examples.get(0).value() != null) {
        return examples.get(0).value();
      }
    }
    return null;
  }

  @Override
  public String getSchema() {
    return getTypeAsString(typeDeclaration);
  }

  @Override
  public Map<String, List<IParameter>> getFormParameters() {
    Map<String, List<IParameter>> result = new LinkedHashMap<>();

    if (typeDeclaration instanceof ObjectTypeDeclaration) {
      List<TypeDeclaration> parameters = ((ObjectTypeDeclaration) typeDeclaration).properties();
      for (TypeDeclaration parameter : parameters) {
        List<IParameter> list = new ArrayList<>();
        list.add(new ParameterImpl(parameter));
        result.put(parameter.name(), list);
      }
    }

    return result;
  }

  @Override
  public List<IValidationResult> validate(String payload) {
    return typeDeclaration.validate(payload).stream()
        .map(ValidationResultImpl::new)
        .collect(Collectors.toList());
  }

  @Override
  public Object getCompiledSchema() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getInstance() {
    throw new UnsupportedOperationException();
  }
}
