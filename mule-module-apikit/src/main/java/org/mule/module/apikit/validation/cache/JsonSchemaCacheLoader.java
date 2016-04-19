/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import static org.mule.module.apikit.validation.cache.SchemaCacheUtils.resolveJsonSchema;

import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.raml.interfaces.model.IRaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.cache.CacheLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class JsonSchemaCacheLoader extends CacheLoader<String, JsonSchema>
{

    private static final String RESOURCE_PREFIX = "resource:/";

    private IRaml api;

    public JsonSchemaCacheLoader(IRaml api)
    {
        this.api = api;
    }

    @Override
    public JsonSchema load(String schemaLocation) throws IOException
    {
        Object pathOrSchema = resolveJsonSchema(schemaLocation, api);
        if (pathOrSchema instanceof String)
        {
            return parseSchema(resolveLocationIfNecessary(formatUri((String) pathOrSchema)));

        }
        return parseSchema((JsonNode) pathOrSchema);
    }

    /*
     * make the location json schema validator friendly appending
     *  resource:/ if necessary
     */
    private String formatUri(String location)
    {
        URI uri = URI.create(location);

        if (uri.getScheme() == null)
        {
            if (location.charAt(0) == '/')
            {
                location = location.substring(1);
            }

            location = RESOURCE_PREFIX + location;
        }

        return location;
    }

    /*
     * in order to find the resource in the application classpath
     *  the reseource:/ url is translated to a file:/ url
     */
    private String resolveLocationIfNecessary(String path)
    {
        URI uri = URI.create(path);

        String scheme = uri.getScheme();
        if (scheme == null || "resource".equals(scheme))
        {
            return openSchema(uri.getPath()).toString();
        }
        return path;
    }

    private URL openSchema(String path)
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null && path.startsWith("/"))
        {
            return openSchema(path.substring(1));
        }

        return url;
    }

    private JsonSchema parseSchema(JsonNode jsonNode)
    {
        try
        {
            return getSchemaFactory().getJsonSchema(jsonNode);
        }
        catch (ProcessingException e)
        {
            throw new ApikitRuntimeException(e);
        }
    }

    private JsonSchema parseSchema(String uri)
    {
        try
        {
            return getSchemaFactory().getJsonSchema(uri);
        }
        catch (ProcessingException e)
        {
            throw new ApikitRuntimeException(e);
        }
    }

    private JsonSchemaFactory getSchemaFactory()
    {
        ValidationConfiguration validationCfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV3).freeze();
        return JsonSchemaFactory.newBuilder().setLoadingConfiguration(LoadingConfiguration.byDefault()).setValidationConfiguration(validationCfg).freeze();
    }
}
