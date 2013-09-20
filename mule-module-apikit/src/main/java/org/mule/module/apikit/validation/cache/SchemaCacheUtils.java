package org.mule.module.apikit.validation.cache;

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

    public static String resolveSchema(String schemaCacheKey, Raml api)
    {
        String[] path = schemaCacheKey.split(SEPARATOR);
        Action action = api.getResource(path[0]).getAction(path[1]);
        MimeType mimeType = action.getBody().get(path[2]);
        String schema = mimeType.getSchema();
        //check global schemas
        if (api.getSchemas().containsKey(schema))
        {
            schema = api.getSchemas().get(schema);
        }
        return schema;
    }
}
