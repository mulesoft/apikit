/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.metadata.model;

import org.mule.module.metadata.raml.RamlApiWrapper;

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
