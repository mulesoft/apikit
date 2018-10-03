/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.implv2.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.mule.raml.implv2.v10.model.RamlImpl10V2.getTypeAsString;

public class MimeTypeImpl implements IMimeType {

  private TypeDeclaration typeDeclaration;
  private Optional<String> typeAsString;

  public MimeTypeImpl(TypeDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;
  }

  @Override
  public String getType() {
    return typeDeclaration.name();
  }

  @Override
  public String getExample() {
    final ExampleSpec example = getExample(typeDeclaration);
    return example != null ? example.value() : null;
  }

  private static ExampleSpec getExample(TypeDeclaration typeDeclaration) {
    final ExampleSpec localExample = getExampleFromLocal(typeDeclaration);
    if (localExample != null)
      return localExample;

    if (typeDeclaration instanceof ObjectTypeDeclaration) {
      return getExampleFromParent((ObjectTypeDeclaration) typeDeclaration);
    }

    if (typeDeclaration instanceof UnionTypeDeclaration) {
      final List<TypeDeclaration> parentTypes = ((UnionTypeDeclaration) typeDeclaration).of();
      for (TypeDeclaration parentType : parentTypes) {
        final ExampleSpec example = getExample(parentType);
        if (example != null)
          return example;
      }
    }

    return null;
  }

  private static ExampleSpec getExampleFromLocal(TypeDeclaration typeDeclaration) {
    final ExampleSpec example = typeDeclaration.example();
    if (example != null && example.value() != null)
      return example;

    List<ExampleSpec> examples = typeDeclaration.examples();
    if (examples != null && !examples.isEmpty()) {
      final List<ExampleSpec> nonNullExamples = examples.stream().filter(e -> e.value() != null).collect(toList());
      if (!nonNullExamples.isEmpty())
        return examples.get(0);
    }

    return null;
  }

  private static ExampleSpec getExampleFromParent(ObjectTypeDeclaration typeDeclaration) {
    if (!hasLocalProperties(typeDeclaration)) {
      for (TypeDeclaration declaration : typeDeclaration.parentTypes()) {
        final ExampleSpec parentExample = getExample(declaration);
        if (parentExample != null)
          return parentExample;
      }
    }
    return null;
  }

  private static boolean hasLocalProperties(ObjectTypeDeclaration typeDeclaration) {
    final List<TypeDeclaration> parentTypes = typeDeclaration.parentTypes();
    if (parentTypes.isEmpty())
      return true;

    final int propertiesCount = typeDeclaration.properties().size();

    return parentTypes.stream()
        .anyMatch(t -> t instanceof ObjectTypeDeclaration && ((ObjectTypeDeclaration) t).properties().size() < propertiesCount);
  }

  @Override
  public String getSchema() {
    if (typeAsString == null) {
      typeAsString = ofNullable(getTypeAsString(typeDeclaration));
    }
    return typeAsString.orElse(null);
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
        .collect(toList());
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
