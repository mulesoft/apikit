/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.util.StringUtils;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;

public class SchemaCacheUtils
{

    private static final String SEPARATOR = ",";
    private static final String EMPTY_NAMESPACE = "__(empty)__";

    public static String getSchemaCacheKey(String nameSpace, Action action, String mimeTypeName)
    {
        StringBuilder key = new StringBuilder(StringUtils.isBlank(nameSpace) ? EMPTY_NAMESPACE : nameSpace);
        key.append(SEPARATOR).append(action.getResource().getUri());
        key.append(SEPARATOR).append(action.getType());
        key.append(SEPARATOR).append(mimeTypeName);
        return key.toString();
    }

    public static String resolveSchema(String schemaCacheKey, Raml api)
    {
        String[] path = schemaCacheKey.split(SEPARATOR);
        Action action = api.getResource(path[1]).getAction(path[2]);
        MimeType mimeType = action.getBody().get(path[3]);
        String schema = mimeType.getSchema();
        //check global schemas
        if (api.getConsolidatedSchemas().containsKey(schema))
        {
            schema = api.getConsolidatedSchemas().get(schema);
        }
        return schema;
    }
}
