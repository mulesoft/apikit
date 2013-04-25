package org.mule.module.apikit.rest.validation.cache;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public final class JsonSchemaCache
{

    private static final String REGISTRY_JSON_SCHEMA_CACHE_KEY = "__restRouterJsonSchemaCache";

    public static LoadingCache<String, JsonSchemaAndNode> getJsonSchemaCache(MuleContext muleContext) throws RegistrationException
    {
        if (muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY) == null)
        {
            LoadingCache<String, JsonSchemaAndNode> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new JsonSchemaCacheLoader(muleContext));

            muleContext.getRegistry().registerObject(REGISTRY_JSON_SCHEMA_CACHE_KEY, transformerCache);
        }

        return muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY);
    }
}
