/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import static org.mule.module.apikit.validation.cache.SchemaCacheUtils.resolveSchema;

import org.mule.api.MuleContext;
import org.mule.module.apikit.validation.io.SchemaResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheLoader;

import java.io.IOException;
import java.io.InputStreamReader;

import org.eel.kitchen.jsonschema.util.JsonLoader;
import org.raml.model.Raml;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchemaAndNode>
{

    private ResourceLoader resourceLoader;
    private Raml api;

    public JsonSchemaCacheLoader(MuleContext muleContext, Raml api)
    {
        this.api = api;
        this.resourceLoader = new SchemaResourceLoader(muleContext.getExecutionClassLoader());
    }

    @Override
    public JsonSchemaAndNode load(String schemaLocation) throws IOException
    {
        JsonNode schemaNode;

        if (schemaLocation.startsWith("/"))
        {
            //inline schema definition
            schemaNode = JsonLoader.fromString(resolveSchema(schemaLocation, api));
        }
        else
        {
            //schema referenced by spring resource
            Resource schemaResource = resourceLoader.getResource(schemaLocation);
            schemaNode = JsonLoader.fromReader(new InputStreamReader(schemaResource.getInputStream()));
        }
        return new JsonSchemaAndNode(schemaNode);
    }

}
