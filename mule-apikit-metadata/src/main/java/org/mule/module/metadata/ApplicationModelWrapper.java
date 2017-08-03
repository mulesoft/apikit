package org.mule.module.metadata;

import org.mule.module.metadata.model.ApikitConfig;
import org.mule.module.metadata.model.Flow;
import org.mule.module.metadata.model.FlowMapping;
import org.mule.module.metadata.raml.RamlApiWrapper;
import org.mule.module.metadata.model.RamlCoordinate;
import org.mule.module.metadata.raml.RamlCoordsSimpleFactory;
import org.mule.module.metadata.raml.RamlHandler;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.config.spring.dsl.model.ApplicationModel;
import org.mule.runtime.config.spring.dsl.model.ComponentModel;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationModelWrapper
{
    private final static String PARAMETER_NAME = "name";
    private final static String PARAMETER_RAML = "raml";
    private final static String PARAMETER_RESOURCE = "resource";
    private final static String PARAMETER_ACTION = "action";
    private final static String PARAMETER_CONTENT_TYPE = "content-type";
    private final static String PARAMETER_FLOW_REF = "flow-ref";

    private ApplicationModel applicationModel;
    private RamlHandler ramlHandler;

    private Map<String, ApikitConfig> apikitConfigMap;
    private Map<String, RamlCoordinate> metadataFlows;

    public ApplicationModelWrapper(ApplicationModel applicationModel, RamlHandler ramlHandler) {
        this.applicationModel = applicationModel;
        this.ramlHandler = ramlHandler;
        initialize();
    }

    private void initialize() {
        findApikitConfigs();
        findAndProcessFlows();
    }

    private void findAndProcessFlows()
    {
        // Finding all valid flows
        List<Flow> flows = findFlows();

        // Creating a Coords Factory, giving the list of all valid config names
        final RamlCoordsSimpleFactory coordsFactory = new RamlCoordsSimpleFactory(getConfigNames());
        Map<String, RamlCoordinate> conventionCoordinates = createCoordinatesForConventionFlows(flows, coordsFactory);
        Map<String, RamlCoordinate> flowMappingCoordinates = createCoordinatesForMappingFlows(flows, coordsFactory);

        // Merging both results
        metadataFlows = Stream.of(conventionCoordinates, flowMappingCoordinates)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void findApikitConfigs() {

        apikitConfigMap = applicationModel.getRootComponentModel().getInnerComponents()
                .stream()
                .filter((element) -> ApikitIdentifiers.isApikitConfig(element.getIdentifier()))
                .map(this::createApikitConfig)
                .collect(Collectors.toMap(ApikitConfig::getName, config -> config));
    }

    private Set<String> getConfigNames()
    {
        return apikitConfigMap.keySet();
    }

    private Map<String, RamlCoordinate> createCoordinatesForMappingFlows(List<Flow> flows, RamlCoordsSimpleFactory coordsFactory)
    {
        return apikitConfigMap.values()
                .stream()
                .flatMap(apikitConfig -> apikitConfig.getFlowMappings().stream())
                .map(flowMapping -> {
                    String flowRefName = flowMapping.getFlowRef();

                    for (Flow f : flows) {
                        if (f.getName().equals(flowRefName)) {
                            return new AbstractMap.SimpleEntry<>(
                                    flowRefName,
                                    coordsFactory.createFromFlowMapping(flowMapping));
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, RamlCoordinate> createCoordinatesForConventionFlows(final List<Flow> flows, final RamlCoordsSimpleFactory coordsFactory)
    {
        return flows
                .stream()
                .map(flow -> {

                    String flowName = flow.getName();

                    return new AbstractMap.SimpleEntry<>(flowName, coordsFactory.createFromFlowName(flowName));
                })
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private ApikitConfig createApikitConfig(ComponentModel unwrappedApikitConfig)
    {
        Map<String, String> parameters = unwrappedApikitConfig.getParameters();
        String configName = parameters.get(PARAMETER_NAME);
        String configRaml = parameters.get(PARAMETER_RAML);

        // Loading the corresponding RAML for this configuration
        IRaml ramlApi = ramlHandler.getRamlApi(configRaml);

        List<FlowMapping> flowMappings = unwrappedApikitConfig.getInnerComponents()
                .stream()
                .filter(config -> ApikitIdentifiers.isFlowMappings(config.getIdentifier()))
                .flatMap(flowMappingsElement -> flowMappingsElement.getInnerComponents().stream())
                .filter(flowMapping -> ApikitIdentifiers.isFlowMapping(flowMapping.getIdentifier()))
                .map(unwrappedFlowMapping -> createFlowMapping(configName, unwrappedFlowMapping))
                .collect(Collectors.toList());

        return new ApikitConfig(configName, configRaml, flowMappings, new RamlApiWrapper(ramlApi));
    }


    private FlowMapping createFlowMapping(String configName, ComponentModel unwrappedFlowMapping)
    {
        Map<String, String> flowMappingParameters = unwrappedFlowMapping.getParameters();

        String resource = flowMappingParameters.get(PARAMETER_RESOURCE);
        String action = flowMappingParameters.get(PARAMETER_ACTION);
        String contentType = flowMappingParameters.get(PARAMETER_CONTENT_TYPE);
        String flowRef = flowMappingParameters.get(PARAMETER_FLOW_REF);

        return new FlowMapping(configName, resource, action, contentType, flowRef);
    }

    public List<Flow> findFlows() {

        return applicationModel.getRootComponentModel().getInnerComponents()
                .stream()
                .filter(element -> ApikitIdentifiers.isFlow(element.getIdentifier()))
                .map(this::createFlow)
                .collect(Collectors.toList());
    }

    private Flow createFlow(ComponentModel componentModel)
    {
        Map<String, String> parameters = componentModel.getParameters();
        String flowName = parameters.get(PARAMETER_NAME);
        return new Flow(flowName);
    }


    public RamlCoordinate getRamlCoordinatesForFlow(String flowName) {
        return metadataFlows.get(flowName);
    }

    public ApikitConfig getApikitConfigWithName(String apikitConfigName) {
        ApikitConfig config = apikitConfigMap.get(apikitConfigName);

        // If the flow is not explicitly naming the config it belongs, we assume there is only one API
        if (config == null) {
            config = apikitConfigMap.values().iterator().next();
        }

        return config;
    }
}
