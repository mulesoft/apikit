/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.interfaces.MetadataSource;
import org.mule.module.apikit.metadata.model.ApikitConfig;
import org.mule.module.apikit.metadata.model.RamlCoordinate;

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
