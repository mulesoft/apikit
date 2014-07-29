/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import org.raml.model.Raml;

public final class JsonSchemaCache
{

    private static final String REGISTRY_JSON_SCHEMA_CACHE_KEY = "__restRouterJsonSchemaCache";

    public static LoadingCache<String, JsonSchemaAndNode> getJsonSchemaCache(MuleContext muleContext, Raml api) throws RegistrationException
    {
        if (muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY) == null)
        {
            LoadingCache<String, JsonSchemaAndNode> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new JsonSchemaCacheLoader(api));

            muleContext.getRegistry().registerObject(REGISTRY_JSON_SCHEMA_CACHE_KEY, transformerCache);
        }

        return muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY);
    }
}
