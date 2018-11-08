/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import javax.annotation.Nullable;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonExampleTypeLoader;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.metadata.xml.api.ModelFactory;
import org.mule.metadata.xml.api.SchemaCollector;
import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.metadata.xml.api.utils.XmlSchemaUtils;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mule.metadata.api.builder.BaseTypeBuilder.create;

class MetadataFactory {

  private static final String MIME_APPLICATION_JSON = "application/json";
  private static final String MIME_APPLICATION_XML = "application/xml";
  private static final String MIME_MULTIPART_FORM_DATA = "multipart/form-data";
  private static final String MIME_APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

  private static final MetadataType DEFAULT_METADATA = create(MetadataFormat.JAVA).anyType().build();
  private static final MetadataType STRING_METADATA = create(MetadataFormat.JAVA).stringType().build();

  private MetadataFactory() {}

  public static MetadataType payloadMetadata(final RamlApiWrapper api, final @Nullable IMimeType body) {
    if (body == null) {
      return MetadataFactory.defaultMetadata();
    }

    final String type = body.getType();
    final String schema = resolveSchema(api, body);
    final String example = body.getExample();

    switch (type) {
      case MIME_APPLICATION_JSON:
        return applicationJsonMetadata(schema, example);
      case MIME_APPLICATION_XML:
        return applicationXmlMetadata(schema, example);
      case MIME_APPLICATION_URL_ENCODED:
        return formMetadata(body.getFormParameters());
      case MIME_MULTIPART_FORM_DATA:
        return formMetadata(body.getFormParameters());
      default:
        return MetadataFactory.defaultMetadata();
    }
  }

  private static String resolveSchema(RamlApiWrapper api, IMimeType body) {
    String schema = body.getSchema();

    // As body.getSchema() can return the name of the schema, null or
    // the schema itself, first we assume that it has the schema name
    // and we try to get the schema def from the api consolidated
    // schemas
    if (api.getConsolidatedSchemas().containsKey(schema)) {
      schema = api.getConsolidatedSchemas().get(schema);
    }

    return schema;
  }

  private static MetadataType formMetadata(Map<String, List<IParameter>> formParameters) {
    return MetadataFactory.fromFormMetadata(formParameters);
  }

  private static MetadataType applicationXmlMetadata(String schema, String example) {
    if (schema != null) {
      return MetadataFactory.fromXSDSchema(schema, example);
    } else if (example != null) {
      return MetadataFactory.fromXMLExample(example);
    }

    return MetadataFactory.defaultMetadata();
  }

  private static MetadataType applicationJsonMetadata(String schema, String example) {
    if (schema != null) {
      return MetadataFactory.fromJsonSchema(schema);
    } else if (example != null) {
      return MetadataFactory.fromJsonExample(example);
    }

    return MetadataFactory.defaultMetadata();
  }


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
  private static MetadataType fromJsonExample(String jsonExample) {
    JsonExampleTypeLoader jsonExampleTypeLoader = new JsonExampleTypeLoader(jsonExample);
    jsonExampleTypeLoader.setFieldRequirementDefault(false);
    Optional<MetadataType> root = jsonExampleTypeLoader.load(null);

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
