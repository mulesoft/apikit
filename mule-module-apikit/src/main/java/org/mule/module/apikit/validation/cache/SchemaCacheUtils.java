/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.module.apikit.exception.ApikitRuntimeException;

import com.github.fge.jackson.JsonLoader;

import java.io.IOException;

import javax.xml.validation.Schema;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;

public class SchemaCacheUtils
{

    private static final String SEPARATOR = ",";

    public static String getSchemaCacheKey(Action action, String mimeTypeName)
    {
        StringBuilder key = new StringBuilder(action.getResource().getUri());
        key.append(SEPARATOR).append(action.getType());
        key.append(SEPARATOR).append(mimeTypeName);
        return key.toString();
    }

    /**
     * Returns the compiled representation of an XML schema.
     */
    public static Schema resolveXmlSchema(String schemaCacheKey, Raml api)
    {
        MimeType mimeType = getMimeType(schemaCacheKey, api);

        Object compiledSchema = mimeType.getCompiledSchema();
        if (compiledSchema != null && compiledSchema instanceof Schema)
        {
            return (Schema) compiledSchema;
        }

        String schema = mimeType.getSchema();

        //check global schemas
        if (api.getConsolidatedSchemas().containsKey(schema))
        {
            compiledSchema = api.getCompiledSchemas().get(schema);
            if (compiledSchema != null && compiledSchema instanceof Schema)
            {
                return (Schema) compiledSchema;
            }
        }
        throw new ApikitRuntimeException("XML Schema could not be resolved for key: " + schemaCacheKey);
    }

    private static MimeType getMimeType(String schemaCacheKey, Raml api)
    {
        String[] path = schemaCacheKey.split(SEPARATOR);
        Action action = api.getResource(path[0]).getAction(path[1]);
        return action.getBody().get(path[2]);
    }

    /**
     * may return either a string representing the path to the schema
     * or a JsonNode for inline schema definitions
     */
    public static Object resolveJsonSchema(String schemaCacheKey, Raml api)
    {
        MimeType mimeType = getMimeType(schemaCacheKey, api);
        String path = (String) mimeType.getCompiledSchema();
        String schemaOrGlobalReference = mimeType.getSchema();

        try
        {
            //check global schemas
            if (api.getConsolidatedSchemas().containsKey(schemaOrGlobalReference))
            {
                path = (String) api.getCompiledSchemas().get(schemaOrGlobalReference);
                if (path != null)
                {
                    return path;
                }
                return JsonLoader.fromString(api.getConsolidatedSchemas().get(schemaOrGlobalReference));
            }

            if (path != null)
            {
                return path;
            }
            return JsonLoader.fromString(schemaOrGlobalReference);
        }
        catch (IOException e)
        {
            throw new ApikitRuntimeException("Json Schema could not be resolved for key: " + schemaCacheKey, e);
        }
    }

}
