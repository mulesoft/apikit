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
