package org.mule.module.metadata;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.metadata.interfaces.ResourceLoader;
import org.mule.module.metadata.model.ApikitConfig;
import org.mule.module.metadata.model.RamlCoordinate;
import org.mule.runtime.config.spring.dsl.model.ApplicationModel;

import java.util.Map;
import java.util.Optional;

public class MetadataHandler
{

    private Map<String, RamlCoordinate> ramlCoordinates;
    private Map<String, ApikitConfig> apikitConfigs;


    public MetadataHandler(ApplicationModel applicationModel, ResourceLoader resourceLoader) {
        ApplicationModelWrapper modelWrapper = new ApplicationModelWrapper(applicationModel, resourceLoader);

        apikitConfigs = modelWrapper.getConfigs();
        ramlCoordinates = modelWrapper.getRamlCoordinates();
    }

    public Optional<FunctionType> getMetadataForFlow(String flowName) {

        // Getting the RAML Coordinate for the specified flowName
        RamlCoordinate coordinate = ramlCoordinates.get(flowName);

        if (coordinate == null) {
            System.out.println("[ ERROR ] There is no metadata for flow " + flowName); // TODO: REPLACE WITH API FUNCTION
            return Optional.empty();
        }

        // If there exists metadata for the flow, we get the Api
        ApikitConfig api = apikitConfigs.get(coordinate.getConfigName());
        if (api == null) {
            System.out.println("[ WARNING ] Flow " + flowName + "doesn't belong to any config, assuming there is only one");
            api = apikitConfigs.values().iterator().next();
        }

        return api.getMetadata(coordinate);
    }
}
