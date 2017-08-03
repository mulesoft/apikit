package org.mule.module.metadata;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.metadata.interfaces.MetadataSource;
import org.mule.module.metadata.model.ApikitConfig;
import org.mule.module.metadata.model.RamlCoordinate;

import java.util.Optional;

public class MetadataHandler
{

    private ApplicationModelWrapper applicationModelWrapper;

    public MetadataHandler(ApplicationModelWrapper applicationModelWrapper) {
        this.applicationModelWrapper = applicationModelWrapper;
    }

    public Optional<FunctionType> getMetadataForFlow(String flowName) {

        // Getting the RAML Coordinate for the specified flowName
        RamlCoordinate coordinate = applicationModelWrapper.getRamlCoordinatesForFlow(flowName);

        if (coordinate == null) {
            System.out.println("[ ERROR ] There is no metadata for flow " + flowName); // TODO: REPLACE WITH API FUNCTION
            return Optional.empty();
        }

        // If there exists metadata for the flow, we get the Api
        ApikitConfig api = applicationModelWrapper.getApikitConfigWithName(coordinate.getConfigName());
        MetadataSource metadataSource = api.getApi().getActionForCoordinate(coordinate);
        return metadataSource.getMetadata();
    }
}
