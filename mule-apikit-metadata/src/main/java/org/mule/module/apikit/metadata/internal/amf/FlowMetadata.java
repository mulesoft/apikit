/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.Payload;
import amf.client.model.domain.Request;
import amf.client.model.domain.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.CertificateFields;
import org.mule.module.apikit.metadata.internal.model.HttpRequestAttributesFields;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
  private static final String STATUS_CODE_200 = "200";

  final private EndPoint endPoint;
  final private Operation operation;
  final private ApiCoordinate coordinate;
  final private Map<String, Parameter> baseUriParameters;
  final private Notifier notifier;

  FlowMetadata(final EndPoint endPoint, final Operation operation, final ApiCoordinate coordinate,
               final Map<String, Parameter> baseUriParameters,
               Notifier notifier) {
    this.endPoint = endPoint;
    this.operation = operation;
    this.coordinate = coordinate;
    this.baseUriParameters = baseUriParameters;
    this.notifier = notifier;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType input = inputMetadata(operation, coordinate, baseUriParameters);
    final MuleEventMetadataType output = outputMetadata(operation, coordinate);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, input)
        .returnType(output)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(final Operation operation, final ApiCoordinate coordinate,
                                              final Map<String, Parameter> baseUriParameters) {

    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(operation, coordinate))
        .attributes(getInputAttributes(operation, baseUriParameters)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(final Operation operation, final ApiCoordinate coordinate) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(operation, coordinate)).build();

    // TODO I' generating a compatible metadata with previous version
    return new MuleEventMetadataTypeBuilder().message(message)
        .addVariable("outboundHeaders", getOutputHeaders(operation).build())
        .addVariable("httpStatus", MetadataFactory.stringMetadata()).build();
  }

  private MetadataType getInputPayload(final Operation operation, final ApiCoordinate coordinate) {

    final Request request = operation.request();
    if (request == null)
      return MetadataFactory.defaultMetadata();

    final List<Payload> payloads = request.payloads();
    final Optional<Payload> payload = findPayload(payloads, coordinate.getMediaType());
    return payload.map(p -> metadata(p, coordinate, "input")).orElseGet(MetadataFactory::defaultMetadata);
  }

  private MetadataType getOutputPayload(final Operation operation, final ApiCoordinate coordinate) {

    final Optional<Response> response = findFirstResponse(operation);
    if (!response.isPresent())
      return MetadataFactory.defaultMetadata();
    final List<Payload> payloads = response.get().payloads();
    final Optional<Payload> payload = findPayload(payloads, coordinate.getMediaType());

    return payload.map(p -> metadata(p, coordinate, "output")).orElseGet(MetadataFactory::defaultMetadata);
  }

  private ObjectTypeBuilder getOutputHeaders(final Operation operation) {
    final List<Parameter> headers = findFirstResponse(operation).map(Response::headers).orElse(emptyList());

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    headers.forEach(header -> builder.addField().key(header.name().value().toLowerCase()).value(metadata(header))
        .required(header.required().value()));

    return builder;
  }


  private ObjectType getInputAttributes(final Operation operation, final Map<String, Parameter> baseUrParameters) {

    // TODO I'm generating compatible metadata with test golden files
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_CLIENT_CERTIFICATE.getName())
        .required(false)
        .value(getClientCertificate());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_HEADERS.getName())
        .required(true)
        .value(getInputHeaders(operation));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LISTENER_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_METHOD.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_PARAMS.getName())
        .required(true)
        .value(getQueryParameters(operation));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_STRING.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_RELATIVE_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REMOTE_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_URI.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_SCHEME.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_URI_PARAMS.getName())
        .required(true)
        .value(getUriParameters(endPoint, baseUrParameters));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_VERSION.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LOCAL_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getInputHeaders(final Operation operation) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    final Request request = operation.request();
    if (request != null)
      request.headers().forEach(header -> builder.addField().key(header.name().value()).value(metadata(header))
          .required(header.required().value()));

    return builder;
  }

  private static MetadataType getClientCertificate() {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_PUBLIC_KEY.getName()).value(MetadataFactory.objectMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_TYPE.getName()).value(MetadataFactory.stringMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_ENCODED.getName()).value(MetadataFactory.binaryMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getUriParameters(final EndPoint endPoint, final Map<String, Parameter> baseUriParameters) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    baseUriParameters
        .forEach((name, param) -> builder.addField().key(name).value(metadata(param)).required(param.required().value()));

    //endPoint.parameters().forEach(parameter -> addField(builder, parameter));
    // TODO ALE BUG AMF ????
    addFields(builder, endPoint, endPoint.parameters());

    return builder;
  }

  private ObjectTypeBuilder getQueryParameters(final Operation operation) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    final Request request = operation.request();
    if (request != null)
      request.queryParameters().forEach(param -> builder.addField().key(param.name().value()).value(metadata(param))
          .required(param.required().value()));

    return builder;
  }

  private void addFields(final ObjectTypeBuilder builder, final EndPoint endPoint, final List<Parameter> parameters) {

    if (!parameters.isEmpty()) {
      final String path = endPoint.path().value();
      final String[] split = path.split("/");
      final List<String> keys = Arrays.stream(split).filter(s -> s.startsWith("{"))
          .map(s -> s.substring(1, s.length() - 1))
          .collect(toList());

      final Map<String, Parameter> parametersMap =
          parameters.stream()
              .collect(Collectors.toMap(p -> p.name().value(), Function.identity()));

      keys.forEach(key -> builder.addField().key(key).value(metadata(parametersMap.get(key)))
          .required(parametersMap.get(key).required().value()));
    }
  }

  private static Optional<Response> findFirstResponse(final Operation operation) {
    return getResponse(operation, STATUS_CODE_200);
  }

  private static Optional<Response> getResponse(final Operation operation, final String statusCode) {
    return operation.responses().stream()
        .filter(response -> statusCode.equals(response.statusCode().value()) && !response.payloads().isEmpty())
        .findFirst();
  }

  private static Optional<Payload> findPayload(final List<Payload> payloads, final String mediaType) {

    if (payloads.isEmpty())
      return empty();

    return payloads.size() == 1 || mediaType == null ? of(payloads.get(0))
        : payloads.stream().filter(p -> p.mediaType().value().equalsIgnoreCase(mediaType)).findFirst();
  }

  private MetadataType metadata(final Parameter parameter) {
    try {
      return MetadataFactory.from(parameter.schema());
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve metadata for parameter '%s'\nDetails: %s", parameter.name(),
                           e.getMessage()));
    }
    return MetadataFactory.defaultMetadata();
  }

  private MetadataType metadata(final Payload payload, ApiCoordinate coordinate,
                                String payloadDescription) {
    try {
      return MetadataFactory.from(payload.schema());
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve %s payload metadata for flow '%s'.\nDetails: %s", payloadDescription,
                           coordinate.getFlowName(), e.getMessage()));
      return MetadataFactory.defaultMetadata();
    }
  }

}
