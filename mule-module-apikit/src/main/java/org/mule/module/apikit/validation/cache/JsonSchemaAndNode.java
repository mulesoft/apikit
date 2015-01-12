/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.module.apikit.exception.ApikitRuntimeException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public final class JsonSchemaAndNode
{

    private final JsonSchema jsonSchema;
    private final JsonNode jsonNode;

    public JsonSchemaAndNode(JsonNode jsonNode)
    {
        this.jsonNode = jsonNode;
        try
        {
            ValidationConfiguration validationCfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV3).freeze();
            JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder().setValidationConfiguration(validationCfg).freeze();
            this.jsonSchema = jsonSchemaFactory.getJsonSchema(jsonNode);
        }
        catch (ProcessingException e)
        {
            throw new ApikitRuntimeException(e);
        }
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
