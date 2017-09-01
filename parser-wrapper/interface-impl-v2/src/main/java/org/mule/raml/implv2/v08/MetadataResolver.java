/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.raml.v2.api.model.v08.parameters.Parameter;

import java.util.Optional;

public class MetadataResolver {

  private MetadataResolver() {}

  public static Optional<MetadataType> resolve(Parameter declaration) {
    return Optional.of(stringType());
  }

  public static MetadataType stringType() {
    return STRING_METADATA_TYPE;
  }

  private static final MetadataType STRING_METADATA_TYPE = BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build();

}
