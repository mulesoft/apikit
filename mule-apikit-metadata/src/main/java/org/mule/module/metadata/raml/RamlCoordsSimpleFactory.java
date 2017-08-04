package org.mule.module.metadata.raml;

import org.mule.module.metadata.model.FlowMapping;
import org.mule.module.metadata.model.RamlCoordinate;

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
//            throw new IllegalStateException("flowName must have at least resourceName and methodName");
        }

        String flowMethodName = parts[0];
        String flowResourceName = parts[1];
        String flowMediaType = null;
        String flowApiConfigName = null;

        // Can be method:resource:configName or method:resource:mediaType
        if (parts.length == 3) {

            if (apiConfigNames.contains(parts[2])) {
                // El tercer parámetro es el nombre de la api a la que pertenece
                flowApiConfigName = parts[2];
            } else {
                // Es el mediaType
                flowMediaType = parts[2];
            }
        }

        // Full example
        if (parts.length == 4) {
            flowMediaType = parts[2];
            flowApiConfigName = parts[3]; // TODO: 7/19/17 VALIDAR?
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