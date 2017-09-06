/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.BooleanTypeBuilder;
import org.mule.metadata.api.builder.NumberTypeBuilder;
import org.mule.metadata.api.builder.StringTypeBuilder;
import org.mule.metadata.api.builder.TypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.raml.model.parameter.AbstractParam;

import java.util.List;
import java.util.Optional;

public class MetadataResolver {


  private MetadataResolver() {}

  public static Optional<MetadataType> resolve(AbstractParam param) {
    final BaseTypeBuilder rootBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA);


    final TypeBuilder builder;

    switch (param.getType()) {
      case NUMBER:
        builder = handle(rootBuilder.numberType(), param);
        break;
      case INTEGER:
        builder = handle(rootBuilder.numberType().integer(), param);
        break;
      case BOOLEAN:
        builder = handle(rootBuilder.booleanType(), param);
        break;
      case STRING:
      case DATE:
      case FILE:
        builder = handle(rootBuilder.stringType(), param);
        break;
      default:
        builder = handle(rootBuilder.stringType(), param);
    }

    final MetadataType metadataType = param.isRepeat() ? rootBuilder.arrayType().of(builder.build()).build() : builder.build();
    return Optional.of(metadataType);
  }

  private static TypeBuilder handle(BooleanTypeBuilder builder, AbstractParam param) {
    Optional.ofNullable(param.getDefaultValue()).ifPresent(builder::defaultValue);

    return builder;
  }

  private static TypeBuilder handle(StringTypeBuilder builder, AbstractParam param) {
    final List<String> enumValues = param.getEnumeration();
    if (enumValues != null && !enumValues.isEmpty()) {
      builder.enumOf(enumValues.toArray(new String[enumValues.size()]));
    }

    final Optional<Integer> minLength = Optional.ofNullable(param.getMinLength());
    final Optional<Integer> maxLength = Optional.ofNullable(param.getMaxLength());
    if (minLength.isPresent() && maxLength.isPresent()) {
      builder.boundary(minLength.get(), maxLength.get());
    } else {
      maxLength.ifPresent(builder::length);
    }

    Optional.ofNullable(param.getPattern()).ifPresent(builder::pattern);
    Optional.ofNullable(param.getDefaultValue()).ifPresent(builder::defaultValue);

    return builder;
  }

  private static TypeBuilder handle(NumberTypeBuilder builder, AbstractParam param) {
    final Optional<Number> maximum = Optional.ofNullable(param.getMaximum());
    final Optional<Number> minimum = Optional.ofNullable(param.getMinimum());
    if (maximum.isPresent() && minimum.isPresent()) {
      builder.range(minimum.get(), maximum.get());
    }

    Optional.ofNullable(param.getDefaultValue()).ifPresent(builder::defaultValue);

    return builder;
  }

  public static MetadataType stringType() {
    return STRING_METADATA_TYPE;
  }

  private static final MetadataType STRING_METADATA_TYPE = BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build();

}
