/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.config;

import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.Console;
import org.mule.module.apikit.FlowMapping;
import org.mule.module.apikit.FlowMappings;
import org.mule.module.apikit.Router;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinition;
import org.mule.runtime.dsl.api.component.ComponentBuildingDefinitionProvider;

import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration;
import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static org.mule.runtime.dsl.api.component.TypeDefinition.fromType;
import static org.mule.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;

import java.util.ArrayList;
import java.util.List;

public class ApikitDefinitionProvider implements ComponentBuildingDefinitionProvider
{

    @Override
    public void init()
    {

    }

    @Override
    public List<ComponentBuildingDefinition> getComponentBuildingDefinitions()
    {
        ComponentBuildingDefinition.Builder baseDefinition = new ComponentBuildingDefinition.Builder().withNamespace(ApikitXmlNamespaceInfoProvider.APIKIT_NAMESPACE);

        List<ComponentBuildingDefinition> definitions = new ArrayList<>();

        definitions.add(baseDefinition.copy().withIdentifier("config")
                                .withTypeDefinition(fromType(Configuration.class))
                                .withSetterParameterDefinition("name", fromSimpleParameter("name").build())
                                .withSetterParameterDefinition("raml", fromSimpleParameter("raml").build())
                                .withSetterParameterDefinition("outboundHeadersMapName", fromSimpleParameter("outboundHeadersMapName").build())
                                .withSetterParameterDefinition("httpStatusVarName", fromSimpleParameter("httpStatusVarName").build())
                                .withSetterParameterDefinition("keepRamlBaseUri", fromSimpleParameter("keepRamlBaseUri").build())
                                .withSetterParameterDefinition("disableValidations", fromSimpleParameter("disableValidations").build())
                                .withSetterParameterDefinition("flowMappings", fromChildConfiguration(FlowMappings.class).build())
                                .build());

        definitions.add(baseDefinition.copy().withIdentifier("flow-mappings")
                                .withTypeDefinition(fromType(FlowMappings.class))
                                .withSetterParameterDefinition("flowMappings", fromChildCollectionConfiguration(FlowMapping.class).build())
                                .build());

        definitions.add(baseDefinition.copy().withIdentifier("flow-mapping")
                                .withTypeDefinition(fromType(FlowMapping.class))
                                .withSetterParameterDefinition("resource", fromSimpleParameter("resource").build())
                                .withSetterParameterDefinition("action", fromSimpleParameter("action").build())
                                .withSetterParameterDefinition("contentType", fromSimpleParameter("content-type").build())
                                .withSetterParameterDefinition("flowRef", fromSimpleParameter("flow-ref").build())
                                .build());

        definitions.add(baseDefinition.copy().withIdentifier("router")
                                .withTypeDefinition(fromType(Router.class))
                                .withSetterParameterDefinition("name", fromSimpleParameter("name").build())
                                .withSetterParameterDefinition("configRef", fromSimpleParameter("config-ref").build()).build());

        definitions.add(baseDefinition.copy().withIdentifier("console")
                                .withTypeDefinition(fromType(Console.class))
                                .withSetterParameterDefinition("name", fromSimpleParameter("name").build())
                                .withSetterParameterDefinition("configRef", fromSimpleParameter("config-ref").build()).build());
        return definitions;
    }
}
