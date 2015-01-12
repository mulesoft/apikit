/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import static org.mule.module.apikit.validation.cache.SchemaCacheUtils.resolveSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.google.common.cache.CacheLoader;

import java.io.IOException;

import org.raml.model.Raml;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchemaAndNode>
{

    private Raml api;

    public JsonSchemaCacheLoader(Raml api)
    {
        this.api = api;
    }

    @Override
    public JsonSchemaAndNode load(String schemaLocation) throws IOException
    {
        JsonNode schemaNode = JsonLoader.fromString(resolveSchema(schemaLocation, api));
        return new JsonSchemaAndNode(schemaNode);
    }

}
