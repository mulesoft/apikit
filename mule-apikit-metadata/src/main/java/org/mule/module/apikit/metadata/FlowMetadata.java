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
import org.mule.metadata.message.api.MessageMetadataType;
import org.mule.metadata.message.api.MessageMetadataTypeBuilder;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.metadata.message.api.MuleEventMetadataTypeBuilder;
import org.mule.module.apikit.metadata.interfaces.MetadataSource;
import org.mule.module.apikit.metadata.model.Payload;
import org.mule.module.apikit.metadata.model.RamlCoordinate;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static org.mule.module.apikit.metadata.MetadataFactory.stringMetadata;

public class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
  private static final String ATTRIBUTES_QUERY_PARAMETERS = "queryParameters";
  private static final String ATTRIBUTES_HEADERS = "headers";
  private static final String ATTRIBUTES_URI_PARAMETERS = "uriParameters";

  final private IAction action;
  final private RamlCoordinate coordinate;
  final private Map<String, IParameter> baseUriParameters;
  final private String httpStatusVar;
  final private String outboundHeadersVar;

  public FlowMetadata(IAction action, RamlCoordinate coordinate, Map<String, IParameter> baseUriParameters, String httpStatusVar,
                      String outboundHeadersVar) {
    this.action = action;
    this.coordinate = coordinate;
    this.baseUriParameters = baseUriParameters;
    this.httpStatusVar = httpStatusVar;
    this.outboundHeadersVar = outboundHeadersVar;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType inputEvent = inputMetadata(action, coordinate, baseUriParameters);
    final MuleEventMetadataType outputEvent = outputMetadata(action, outboundHeadersVar, httpStatusVar);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, inputEvent)
        .returnType(outputEvent)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(IAction action, RamlCoordinate coordinate,
                                              Map<String, IParameter> baseUriParameters) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(action, coordinate))
        .attributes(getInputAttributes(action, baseUriParameters)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(IAction action, String outboundHeadersVar, String httpStatusVar) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(action)).build();

    return new MuleEventMetadataTypeBuilder().message(message)
        .addVariable(outboundHeadersVar, getOutputHeadersMetadata(action).build())
        .addVariable(httpStatusVar, stringMetadata()).build();
  }

  private ObjectTypeBuilder getOutputHeadersMetadata(IAction action) {
    final Map<String, IParameter> headers = findFirstResponse(action).map(IResponse::getHeaders).orElse(emptyMap());

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    headers.forEach((name, value) -> builder.addField().key(name).value(value.getMetadata()));

    return builder;
  }

  private Optional<IResponse> findFirstResponse(IAction action) {
    final Optional<IResponse> response = getResponse(action, "200");

    if (response.isPresent())
      return response;

    return action.getResponses().keySet().stream()
        .map(code -> getResponse(action, code))
        .filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

  private Optional<IResponse> getResponse(IAction action, String statusCode) {
    return Optional.ofNullable(action.getResponses().get(statusCode)).filter(IResponse::hasBody);
  }

  private ObjectTypeBuilder getQueryParameters(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getQueryParameters().forEach(
                                        (key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectTypeBuilder getHeaders(IAction action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getHeaders().forEach(
                                (key, value) -> builder.addField().key(key).value(value.getMetadata()));

    return builder;
  }

  private ObjectType getInputAttributes(IAction action, Map<String, IParameter> baseUriParameters) {

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(ATTRIBUTES_QUERY_PARAMETERS)
        .value(getQueryParameters(action));
    builder.addField()
        .key(ATTRIBUTES_HEADERS)
        .value(getHeaders(action));
    builder.addField()
        .key(ATTRIBUTES_URI_PARAMETERS)
        .value(getUriParameters(action, baseUriParameters));

    return builder.build();
  }

  private ObjectTypeBuilder getUriParameters(IAction action, Map<String, IParameter> baseUriParameters) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    baseUriParameters.forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata()));
    action.getResource().getResolvedUriParameters()
        .forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata()));

    return builder;
  }

  private MetadataType getOutputPayload(IAction action) {
    final Optional<Collection<IMimeType>> mimeTypes = findFirstResponse(action)
        .map(response -> response.getBody().values());

    @Nullable
    final IMimeType mimeType;
    if (mimeTypes.isPresent()) {
      mimeType = mimeTypes.get().stream().findFirst().orElse(null);
    } else {
      mimeType = null;
    }

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
