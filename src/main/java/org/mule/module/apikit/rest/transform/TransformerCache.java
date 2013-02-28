package org.mule.module.apikit.rest.transform;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.Transformer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public final class TransformerCache
{

    private static final String REGISTRY_TRANSFORMER_CACHE_KEY = "__restRouterTransformerCache";

    public static LoadingCache<DataTypePair, Transformer> getTransformerCache(MuleContext muleContext) throws RegistrationException
    {
        if (muleContext.getRegistry().get(REGISTRY_TRANSFORMER_CACHE_KEY) == null)
        {
            LoadingCache<DataTypePair, Transformer> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new TransformerCacheLoader(muleContext));

            muleContext.getRegistry().registerObject(REGISTRY_TRANSFORMER_CACHE_KEY, transformerCache);
        }

        return muleContext.getRegistry().get(REGISTRY_TRANSFORMER_CACHE_KEY);
    }
}
