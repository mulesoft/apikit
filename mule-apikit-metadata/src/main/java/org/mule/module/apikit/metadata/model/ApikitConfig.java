/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.model;

import org.mule.module.apikit.metadata.raml.RamlApiWrapper;
import org.mule.raml.interfaces.model.IRaml;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ApikitConfig
{
    final private String name;
    final private String raml;
    final private List<FlowMapping> flowMappings;
    final private Supplier<Optional<IRaml>> apiSupplier;
    private RamlApiWrapper ramlApi;

    public ApikitConfig(String name, String raml, List<FlowMapping> flowMappings, Supplier<Optional<IRaml>> apiSupplier) {
        this.name = name;
        this.raml = raml;
        this.flowMappings = flowMappings;
        this.apiSupplier = apiSupplier;
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

    public Optional<RamlApiWrapper> getApi() {
        if (ramlApi == null) {
            ramlApi = apiSupplier.get().map(RamlApiWrapper::new).orElse(null);
        }
        return ofNullable(ramlApi);
    }

}
