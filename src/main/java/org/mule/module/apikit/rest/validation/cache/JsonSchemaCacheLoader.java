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

import heaven.model.Action;
import heaven.model.Heaven;
import heaven.model.MimeType;
import org.eel.kitchen.jsonschema.util.JsonLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchemaAndNode>
{

    private ResourceLoader resourceLoader;
    private Heaven api;

    public JsonSchemaCacheLoader(MuleContext muleContext, Heaven api)
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
            //TODO remove hack to get schema using coords
            String[] path = schemaLocation.split(",");
            Action action = api.getResource(path[0]).getAction(path[1]);
            MimeType mimeType = action.getBody().getMimeTypes().get(path[2]);
            schemaNode = JsonLoader.fromString(mimeType.getSchema());
        }
        else
        {
            //schema referenced by spring resource
            Resource schemaResource = resourceLoader.getResource(schemaLocation);
            schemaNode = JsonLoader.fromReader(new InputStreamReader(schemaResource.getInputStream()));
        }
        if (schemaNode instanceof ObjectNode)
        {
            ((ObjectNode) schemaNode).put("additionalProperties", false);
        }
        return new JsonSchemaAndNode(schemaNode);
    }
}
