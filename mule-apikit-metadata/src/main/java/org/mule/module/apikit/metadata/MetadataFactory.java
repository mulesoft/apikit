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
import org.mule.metadata.json.api.JsonExampleTypeLoader;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.metadata.xml.api.ModelFactory;
import org.mule.metadata.xml.api.SchemaCollector;
import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.metadata.xml.api.utils.XmlSchemaUtils;
import org.mule.raml.interfaces.model.parameter.IParameter;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
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
    final JsonTypeLoader jsonTypeLoader = new JsonTypeLoader(jsonSchema);
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

    JsonExampleTypeLoader jsonExampleTypeLoader = new JsonExampleTypeLoader(jsonExample);
    jsonExampleTypeLoader.setFieldRequirementDefault(false);
    root = jsonExampleTypeLoader.load(null);

    // We didn't managed to parse the schema.
    return root.orElse(defaultMetadata());
  }

  /**
   *
   * @param xsdSchema
   * @param example
   * @return
   */
  public static MetadataType fromXSDSchema(String xsdSchema, String example) {
    final Optional<QName> rootElementName = XmlSchemaUtils.getXmlSchemaRootElementName(singletonList(xsdSchema), example);
    return rootElementName.map(qName -> {
      /*
        See
        https://github.com/mulesoft/metadata-model-api/blob/d1b8147a487fb1986821276cd9fd4bb320124604/metadata-model-raml/src/main/java/org/mule/metadata/raml/api/XmlRamlTypeLoader.java#L58
      */
      final XmlTypeLoader xmlTypeLoader = new XmlTypeLoader(SchemaCollector.getInstance().addSchema("", xsdSchema));
      return xmlTypeLoader.load(qName.toString()).orElse(defaultMetadata());
    }).orElse(defaultMetadata());
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

  public static MetadataType objectMetadata() {
    return create(MetadataFormat.JAVA).objectType().build();
  }

  public static MetadataType binaryMetadata() {
    return create(MetadataFormat.JAVA).binaryType().build();
  }

}
