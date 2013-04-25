package org.mule.module.apikit.rest.validation.cache;

import com.fasterxml.jackson.databind.JsonNode;

import org.eel.kitchen.jsonschema.main.JsonSchema;
import org.eel.kitchen.jsonschema.main.JsonSchemaFactory;

public final class JsonSchemaAndNode
{

    private final JsonSchema jsonSchema;
    private final JsonNode jsonNode;

    public JsonSchemaAndNode(JsonNode jsonNode)
    {
        this.jsonNode = jsonNode;
        this.jsonSchema = JsonSchemaFactory.defaultFactory().fromSchema(jsonNode);
    }

    public JsonSchema getJsonSchema()
    {
        return jsonSchema;
    }

    public JsonNode getJsonNode()
    {
        return jsonNode;
    }
}
