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

    private static final String REGISTRY_JSON_SCHEMA_CACHE_KEY_PREFIX = "__restRouterJsonSchemaCache__";

    public static LoadingCache<String, JsonSchemaAndNode> getJsonSchemaCache(MuleContext muleContext, String configId, Raml api) throws RegistrationException
    {
        String cacheKey = REGISTRY_JSON_SCHEMA_CACHE_KEY_PREFIX + configId;
        if (muleContext.getRegistry().get(cacheKey) == null)
        {
            LoadingCache<String, JsonSchemaAndNode> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new JsonSchemaCacheLoader(api));

            muleContext.getRegistry().registerObject(cacheKey, transformerCache);
        }

        return muleContext.getRegistry().get(cacheKey);
    }
}
