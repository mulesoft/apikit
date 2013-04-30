/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.validation.cache;

import org.mule.api.MuleContext;
import org.mule.module.apikit.rest.validation.io.SchemaResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheLoader;

import java.io.IOException;
import java.io.InputStreamReader;

import org.eel.kitchen.jsonschema.util.JsonLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchemaAndNode>
{

    private ResourceLoader resourceLoader;

    public JsonSchemaCacheLoader(MuleContext muleContext)
    {
        this.resourceLoader = new SchemaResourceLoader(muleContext.getExecutionClassLoader());
    }

    @Override
    public JsonSchemaAndNode load(String schemaLocation) throws IOException
    {
        Resource schemaResource = resourceLoader.getResource(schemaLocation);
        JsonNode schemaNode = JsonLoader.fromReader(new InputStreamReader(schemaResource.getInputStream()));
        if (schemaNode instanceof ObjectNode)
        {
            ((ObjectNode) schemaNode).put("additionalProperties", false);
        }
        return new JsonSchemaAndNode(schemaNode);
    }
}
