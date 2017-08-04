/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.mule.module.apikit.metadata.model.FlowMapping;
import org.mule.module.apikit.metadata.model.RamlCoordinate;

import java.util.Set;

public class RamlCoordsSimpleFactory
{
    private Set<String> apiConfigNames;

    public RamlCoordsSimpleFactory(Set<String> apiConfigNames) {
        this.apiConfigNames = apiConfigNames;
    }

    public RamlCoordinate createFromFlowName(String flowName) {

        String[] parts = flowName.split(":");

        if (parts.length < 2 || parts.length > 4) {
            return null;
        }

        String flowMethodName = parts[0];
        String flowResourceName = parts[1];
        String flowMediaType = null;
        String flowApiConfigName = null;

        if (parts.length == 3) {

            if (apiConfigNames.contains(parts[2])) {
                flowApiConfigName = parts[2];
            } else {
                flowMediaType = parts[2];
            }
        }

        if (parts.length == 4) {
            flowMediaType = parts[2];
            flowApiConfigName = parts[3];
        }

        return new RamlCoordinate(flowMethodName, flowResourceName, flowMediaType, flowApiConfigName);
    }


    public RamlCoordinate createFromFlowMapping(FlowMapping flowMapping) {

        String configName = flowMapping.getConfigName();
        String action = flowMapping.getAction();
        String resource = flowMapping.getResource();
        String contentType = flowMapping.getContentType();

        return new RamlCoordinate(action, resource, contentType, configName);
    }
}
