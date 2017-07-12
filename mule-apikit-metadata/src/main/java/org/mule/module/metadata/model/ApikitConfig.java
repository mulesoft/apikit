package org.mule.module.metadata.model;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.metadata.RamlApiWrapper;
import org.mule.raml.interfaces.model.IRaml;

import java.util.List;
import java.util.Optional;

public class ApikitConfig
{
    private String name;
    private String raml;
    private List<FlowMapping> flowMappings;
    private RamlApiWrapper ramlResources;

    public ApikitConfig(String name, String raml, List<FlowMapping> flowMappings, IRaml ramlApi) {
        this.name = name;
        this.raml = raml;
        this.flowMappings = flowMappings;
        ramlResources = new RamlApiWrapper(ramlApi);
    }

    public String getName()
    {
        return name;
    }

    public String getRaml()
    {
        return raml;
    }

    public List<FlowMapping> getFlowMappings()
    {
        return flowMappings;
    }

    public Optional<FunctionType> getMetadata(RamlCoordinate coordinate) {
        return ramlResources.getMetadataForCoordinate(coordinate);
    }

}
