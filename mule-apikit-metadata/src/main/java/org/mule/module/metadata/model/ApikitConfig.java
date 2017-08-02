package org.mule.module.metadata.model;

import org.mule.module.metadata.RamlApiWrapper;

import java.util.List;

public class ApikitConfig
{
    private String name;
    private String raml;
    private List<FlowMapping> flowMappings;
    private RamlApiWrapper ramlApi;

    public ApikitConfig(String name, String raml, List<FlowMapping> flowMappings, RamlApiWrapper ramlApi) {
        this.name = name;
        this.raml = raml;
        this.flowMappings = flowMappings;
        this.ramlApi = ramlApi;
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

    public RamlApiWrapper getApi() {
        return ramlApi;
    }

}
