/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.JsonExampleTypeLoader;
import org.mule.metadata.json.JsonTypeLoader;
import org.mule.metadata.xml.ModelFactory;
import org.mule.metadata.xml.XmlTypeLoader;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mule.metadata.api.builder.BaseTypeBuilder.create;

public class MetadataFactory {

  private static final MetadataType DEFAULT_METADATA = create(MetadataFormat.JAVA).anyType().build();
  private static final MetadataType STRING_METADATA = create(MetadataFormat.JAVA).stringType().build();

  private MetadataFactory() {}

  /**
   * Creates metadata from a JSON Schema
   *
   * @param jsonSchema The schema we want to create metadata from
   * @return The metadata if the Schema is valid, null otherwise
   */
  public static MetadataType fromJsonSchema(String jsonSchema) {

    JsonTypeLoader jsonTypeLoader = new JsonTypeLoader(jsonSchema);
    final Optional<MetadataType> root = jsonTypeLoader.load(null);

    // We didn't managed to parse the schema.
    return root.orElse(defaultMetadata());
  }


  /**
   * Creates metadata from the specified JSON Example
   *
   * @param jsonExample
   * @return The metadata if the example is valid, null otherwise
   */
  public static MetadataType fromJsonExample(String jsonExample) {

    Optional<MetadataType> root = Optional.empty();

    try {

      JsonExampleTypeLoader jsonExampleTypeLoader = new JsonExampleTypeLoader(jsonExample);
      root = jsonExampleTypeLoader.load(null);

    } catch (Exception e) {

      System.out.println("[ ERROR ] There was a problem when trying to parse example : " + jsonExample);
    }

    // We didn't managed to parse the schema.
    return root.orElse(defaultMetadata());
  }

  /**
   *
   * @param xsdSchema
   * @return
   */
  public static MetadataType fromXSDSchema(String xsdSchema) {
    // TODO: 7/26/17  
    return defaultMetadata();
  }

  public static MetadataType fromXMLExample(String xmlExample) {

    ModelFactory modelFactory = ModelFactory.fromExample(xmlExample);
    Optional<MetadataType> metadata = new XmlTypeLoader(modelFactory).load(null);

    return metadata.orElse(defaultMetadata());
  }

  public static MetadataType fromFormMetadata(Map<String, List<IParameter>> formParameters) {
    final ObjectTypeBuilder parameters = create(MetadataFormat.JAVA).objectType();

    for (Map.Entry<String, List<IParameter>> entry : formParameters.entrySet()) {
      parameters.addField()
          .key(entry.getKey())
          .value().anyType();
    }

    return parameters.build();
  }

  /**
   * Creates default metadata, that can be of any type
   * @return The newly created MetadataType
   */
  public static MetadataType defaultMetadata() {
    return DEFAULT_METADATA;
  }

  /**
   * Creates metadata to describe an string type
   * @return The newly created MetadataType
   */
  public static MetadataType stringMetadata() {
    return STRING_METADATA;
  }
}
