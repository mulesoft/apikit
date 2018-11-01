/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.module.apikit.metadata.internal.raml.RamlHandler;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.internal.model.ApplicationModel;
import org.mule.runtime.config.internal.model.ComponentModel;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mule.apikit.common.CollectionUtils.merge;
import static org.mule.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

public class ApplicationModelWrapper {

  private final static String PARAMETER_NAME = "name";
  private final static String PARAMETER_RAML = "raml";
  private final static String PARAMETER_OUTPUT_HEADERS_VAR = "outboundHeadersMapName";
  private final static String PARAMETER_HTTP_STATUS_VAR = "httpStatusVarName";
  private final static String PARAMETER_RESOURCE = "resource";
  private final static String PARAMETER_ACTION = "action";
  private final static String PARAMETER_CONTENT_TYPE = "content-type";
  private final static String PARAMETER_FLOW_REF = "flow-ref";

  private static final ComponentIdentifier FLOW = buildFromStringRepresentation("flow");

  private ApplicationModel applicationModel;
  private RamlHandler ramlHandler;
  private Notifier notifier;

  private Map<String, ApikitConfig> configMap;
  private Map<String, ApiCoordinate> metadataFlows;

  public ApplicationModelWrapper(ApplicationModel applicationModel, RamlHandler ramlHandler, Notifier notifier) {
    this.applicationModel = applicationModel;
    this.ramlHandler = ramlHandler;
    this.notifier = notifier;
    initialize();
  }

  private void initialize() {
    loadConfigs();
    loadFlows();
  }

  private void loadFlows() {
    // Finding all valid flows
    final List<Flow> flows = findFlows();

    // Creating a Coords Factory, giving the list of all valid config names
    final ApiCoordinateFactory coordsFactory = new ApiCoordinateFactory(getConfigNames());
    final Map<String, ApiCoordinate> conventionCoordinates = createCoordinatesForConventionFlows(flows, coordsFactory);
    final Map<String, ApiCoordinate> flowMappingCoordinates = createCoordinatesForMappingFlows(flows, coordsFactory);

    // Merging both results
    metadataFlows = merge(asList(conventionCoordinates, flowMappingCoordinates));
  }

  private void loadConfigs() {
    configMap = applicationModel.getRootComponentModel().getInnerComponents().stream()
        .filter(ApikitElementIdentifiers::isApikitConfig)
        .map(this::createApikitConfig)
        .collect(toMap(ApikitConfig::getName, identity()));
  }

  private Set<String> getConfigNames() {
    return configMap.keySet();
  }

  public Collection<ApikitConfig> getConfigurations() {
    return configMap.values();
  }

  private Map<String, ApiCoordinate> createCoordinatesForMappingFlows(List<Flow> flows, ApiCoordinateFactory coordsFactory) {
    final Set<String> flowNames = flows.stream().map(Flow::getName).collect(toSet());

    return configMap.values().stream()
        .flatMap(config -> config.getFlowMappings().stream())
        .filter(mapping -> flowNames.contains(mapping.getFlowRef()))
        .map(coordsFactory::createFromFlowMapping)
        .collect(toMap(ApiCoordinate::getFlowName, identity()));
  }

  private Map<String, ApiCoordinate> createCoordinatesForConventionFlows(final List<Flow> flows,
                                                                         final ApiCoordinateFactory coordsFactory) {
    return flows
        .stream()
        .map(flow -> coordsFactory.createFromFlowName(flow.getName()))
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toMap(ApiCoordinate::getFlowName, identity()));
  }


  private ApikitConfig createApikitConfig(ComponentModel unwrappedApikitConfig) {
    final Map<String, String> parameters = unwrappedApikitConfig.getParameters();
    final String configName = parameters.get(PARAMETER_NAME);
    final String configRaml = parameters.get(PARAMETER_RAML);
    final String outputHeadersVarName = parameters.get(PARAMETER_OUTPUT_HEADERS_VAR);
    final String httpStatusVarName = parameters.get(PARAMETER_HTTP_STATUS_VAR);

    final List<FlowMapping> flowMappings = unwrappedApikitConfig.getInnerComponents()
        .stream()
        .filter(config -> ApikitElementIdentifiers.isFlowMappings(config.getIdentifier()))
        .flatMap(flowMappingsElement -> flowMappingsElement.getInnerComponents().stream())
        .filter(flowMapping -> ApikitElementIdentifiers.isFlowMapping(flowMapping.getIdentifier()))
        .map(unwrappedFlowMapping -> createFlowMapping(configName, unwrappedFlowMapping))
        .collect(toList());

    final RamlHandlerSupplier ramlSupplier = RamlHandlerSupplier.create(configRaml, ramlHandler);

    return new ApikitConfig(configName, configRaml, flowMappings, ramlSupplier, httpStatusVarName, outputHeadersVarName,
                            notifier);
  }

  private static class RamlHandlerSupplier implements Supplier<Optional<IRaml>> {

    private String configRaml;
    private RamlHandler handler;

    private RamlHandlerSupplier(String configRaml, RamlHandler handler) {
      this.configRaml = configRaml;
      this.handler = handler;
    }

    private static RamlHandlerSupplier create(String configRaml, RamlHandler handler) {
      return new RamlHandlerSupplier(configRaml, handler);
    }

    @Override
    public Optional<IRaml> get() {
      return handler.getRamlApi(configRaml);
    }
  }

  private FlowMapping createFlowMapping(String configName, ComponentModel unwrappedFlowMapping) {
    Map<String, String> flowMappingParameters = unwrappedFlowMapping.getParameters();

    String resource = flowMappingParameters.get(PARAMETER_RESOURCE);
    String action = flowMappingParameters.get(PARAMETER_ACTION);
    String contentType = flowMappingParameters.get(PARAMETER_CONTENT_TYPE);
    String flowRef = flowMappingParameters.get(PARAMETER_FLOW_REF);

    return new FlowMapping(configName, resource, action, contentType, flowRef);
  }

  public List<Flow> findFlows() {
    return findFlows(applicationModel);
  }

  public static List<Flow> findFlows(final ApplicationModel applicationModel) {
    return applicationModel.getRootComponentModel().getInnerComponents().stream()
        .filter(ApplicationModelWrapper::isFlow)
        .map(ApplicationModelWrapper::createFlow)
        .collect(toList());
  }

  private static Flow createFlow(ComponentModel componentModel) {
    final Map<String, String> parameters = componentModel.getParameters();
    final String flowName = parameters.get(PARAMETER_NAME);
    return new Flow(flowName);
  }

  private static boolean isFlow(final ComponentModel component) {
    return component.getIdentifier().equals(FLOW);
  }

  public Optional<ApiCoordinate> getRamlCoordinatesForFlow(String flowName) {
    return ofNullable(metadataFlows.get(flowName));
  }

  public Optional<ApikitConfig> getConfig(String configName) {
    if (configMap.isEmpty()) {
      return empty();
    }

    // If the flow is not explicitly naming the config it belongs, we assume there is only one API
    return Optional.of(configMap.getOrDefault(configName, configMap.values().iterator().next()));
  }


}
