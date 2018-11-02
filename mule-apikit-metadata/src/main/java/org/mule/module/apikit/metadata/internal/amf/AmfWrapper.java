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
import amf.client.model.domain.Server;
import amf.client.model.domain.WebApi;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mule.module.apikit.metadata.api.MetadataSource;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.MetadataResolver;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class AmfWrapper implements MetadataResolver {

  private final Map<String, EndPoint> endPoints;
  private final Map<String, Parameter> baseUriParameters;
  private final Notifier notifier;

  public AmfWrapper(final WebApi webApi, final Notifier notifier) {
    endPoints = webApi.endPoints().stream().collect(toMap(endPoint -> endPoint.path().value(), identity()));
    this.baseUriParameters = baseUriParameters(webApi);
    this.notifier = notifier;
  }

  private Map<String, Parameter> baseUriParameters(final WebApi webApi) {
    final List<Server> servers = webApi.servers();
    if (servers.isEmpty())
      return emptyMap();

    final List<Parameter> variables = webApi.servers().get(0).variables();
    return variables.stream().collect(toMap(parameter -> parameter.name().value(), parameter -> parameter));
  }

  public Optional<MetadataSource> getMetadataSource(final ApiCoordinate coordinate, final String httpStatusVar,
                                                    final String outboundHeadersVar) {
    final EndPoint endPoint = endPoints.get(coordinate.getResource());
    if (endPoint == null)
      return Optional.empty();

    final Optional<Operation> operation = operation(endPoint, coordinate);
    return operation.map(op -> new FlowMetadata(endPoint, op, coordinate, baseUriParameters, notifier));
  }

  private Optional<Operation> operation(final EndPoint endPoint, final ApiCoordinate coordinate) {
    return endPoint.operations().stream()
        .filter(op -> op.method().value().equalsIgnoreCase(coordinate.getMethod())).findFirst();
  }
}


