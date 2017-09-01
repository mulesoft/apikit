/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.raml.internal.CustomHandlingTypeDeclarationTypeLoader;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.Optional;

public class MetadataResolver {

  private MetadataResolver() {}

  public static Optional<MetadataType> resolve(TypeDeclaration declaration) {
    final CustomHandlingTypeDeclarationTypeLoader typeLoader = createTypeLoader();

    return typeLoader.load(declaration, null, null);
  }

  private static CustomHandlingTypeDeclarationTypeLoader createTypeLoader() {
    return new CustomHandlingTypeDeclarationTypeLoader(MetadataFormat.JAVA);
  }

  public static MetadataType anyType() {
    return ANY_METADATA_TYPE;
  }

  private static final MetadataType ANY_METADATA_TYPE = BaseTypeBuilder.create(MetadataFormat.JAVA).anyType().build();

}
