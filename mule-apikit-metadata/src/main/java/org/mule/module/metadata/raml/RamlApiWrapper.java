package org.mule.module.metadata.raml;

import org.mule.module.metadata.interfaces.MetadataSource;
import org.mule.module.metadata.FlowMetadata;
import org.mule.module.metadata.model.RamlCoordinate;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;

import java.util.HashMap;
import java.util.Map;

public class RamlApiWrapper
{

    private Map<String, IResource> ramlResources = new HashMap<>();


    public RamlApiWrapper(IRaml ramlApi) {
        collectResourcesRecursively(ramlApi.getResources());
    }

    private void collectResourcesRecursively(Map<String, IResource> resources)
    {
        for (IResource resource : resources.values()) {
            ramlResources.put(resource.getUri(), resource);
            collectResourcesRecursively(resource.getResources());
        }
    }

    public MetadataSource getActionForCoordinate(RamlCoordinate coordinate) {
        IResource resource = ramlResources.get(coordinate.getResource());
        IAction action = resource.getAction(coordinate.getMethod());
        return new FlowMetadata(action, coordinate);
    }
}




