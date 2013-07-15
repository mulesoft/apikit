/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.validation.cache;

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
