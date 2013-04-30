/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.validation.cache;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import javax.xml.validation.Schema;

public final class XmlSchemaCache
{

    private static final String REGISTRY_JSON_SCHEMA_CACHE_KEY = "__restRouterXmlSchemaCache";

    public static LoadingCache<String, Schema> getXmlSchemaCache(MuleContext muleContext) throws RegistrationException
    {
        if (muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY) == null)
        {
            LoadingCache<String, Schema> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new XmlSchemaCacheLoader(muleContext));

            muleContext.getRegistry().registerObject(REGISTRY_JSON_SCHEMA_CACHE_KEY, transformerCache);
        }

        return muleContext.getRegistry().get(REGISTRY_JSON_SCHEMA_CACHE_KEY);
    }
}
