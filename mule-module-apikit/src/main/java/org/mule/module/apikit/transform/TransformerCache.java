/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.Transformer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public final class TransformerCache
{
    final private static Object LOCK = new Object();

    private static final String REGISTRY_TRANSFORMER_CACHE_KEY = "__restRouterTransformerCache";

    public static LoadingCache<DataTypePair, Transformer> getTransformerCache(MuleContext muleContext) throws RegistrationException
    {
        if (muleContext.getRegistry().get(REGISTRY_TRANSFORMER_CACHE_KEY) == null)
        {
            synchronized (LOCK) {
                if (muleContext.getRegistry().get(REGISTRY_TRANSFORMER_CACHE_KEY) == null) {
                    LoadingCache<DataTypePair, Transformer> transformerCache = CacheBuilder.newBuilder()
                            .maximumSize(1000)
                            .build(new TransformerCacheLoader(muleContext));

                    muleContext.getRegistry().registerObject(REGISTRY_TRANSFORMER_CACHE_KEY, transformerCache);
                }
            }
        }

        return muleContext.getRegistry().get(REGISTRY_TRANSFORMER_CACHE_KEY);
    }
}
