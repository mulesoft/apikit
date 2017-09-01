/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.message.MessageMetadataType;
import org.mule.metadata.message.MessageMetadataTypeBuilder;
import org.mule.metadata.message.MuleEventMetadataType;
import org.mule.metadata.message.MuleEventMetadataTypeBuilder;
import org.mule.module.apikit.metadata.interfaces.MetadataSource;
import org.mule.module.apikit.metadata.model.Payload;
import org.mule.module.apikit.metadata.model.RamlCoordinate;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;

import javax.annotation.Nullable;
import java.util.Optional;

import static java.util.Optional.of;

public class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
  private static final String ATTRIBUTES_QUERY_PARAMETERS = "queryParameters";
  private static final String ATTRIBUTES_HEADERS = "headers";
  private static final String ATTRIBUTES_URI_PARAMETERS = "uriParameters";

  final private IAction action;
  final private RamlCoordinate coordinate;

  public FlowMetadata(IAction action, RamlCoordinate coordinate) {
    this.action = action;
    this.coordinate = coordinate;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType inputEvent = inputMetadata(action, coordinate);
    final MuleEventMetadataType outputEvent = outputMetadata(action, coordinate);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, inputEvent)
        .returnType(outputEvent)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(IAction action, RamlCoordinate coordinate) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(action, coordinate))
        .attributes(getInputAttributes(action)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(IAction action, RamlCoordinate coordinate) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(action)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
    // .addVariable("outboundHeadersMapName", getHeadersOutputMetadata()).build();
  }

  private ObjectTypeBuilder getQueryParameters(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getQueryParameters().forEach((key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectTypeBuilder getHeaders(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getHeaders().forEach((key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectType getInputAttributes(IAction action) {

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(ATTRIBUTES_QUERY_PARAMETERS)
        .value(getQueryParameters(action));
    builder.addField()
        .key(ATTRIBUTES_HEADERS)
        .value(getHeaders(action));
    builder.addField()
        .key(ATTRIBUTES_URI_PARAMETERS)
        .value(getUriParameters(action));

    return builder.build();
  }

  private ObjectTypeBuilder getUriParameters(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getResource().getResolvedUriParameters()
        .forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata()));

    return builder;
  }

  private MetadataType getOutputPayload(IAction action) {
    @Nullable
    final IMimeType mimeType = action.getResponses().values().stream()
        .filter(response -> response.getBody() != null)
        .flatMap(response -> response.getBody().values().stream())
        .findFirst().orElse(null);

    return Payload.metadata(mimeType);
  }

  private MetadataType getInputPayload(IAction action, RamlCoordinate coordinate) {
    @Nullable
    IMimeType mimeType = null;

    if (action.hasBody()) {
      if (action.getBody().size() == 1) {
        mimeType = action.getBody().values().iterator().next();
      } else if (coordinate.getMediaType() != null) {
        mimeType = action.getBody().get(coordinate.getMediaType());
      }
    }

    return Payload.metadata(mimeType);
  }
}
