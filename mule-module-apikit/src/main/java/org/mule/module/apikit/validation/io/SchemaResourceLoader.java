/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.io;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class SchemaResourceLoader extends DefaultResourceLoader
{

    public static final String JSON_SCHEMA_PREFIX = "jsonschema:";
    public static final String XML_SCHEMA_PREFIX = "xmlschema:";

    public SchemaResourceLoader(ClassLoader classLoader)
    {
        super(classLoader);
    }

    @Override
    public Resource getResource(String location)
    {
        Assert.notNull(location, "Location must not be null");
        if (location.startsWith(JSON_SCHEMA_PREFIX))
        {
            return new JsonSchemaResource(location.substring(JSON_SCHEMA_PREFIX.length()), getClassLoader());
        }
        else if (location.startsWith(XML_SCHEMA_PREFIX))
        {
            return new XmlSchemaResource(location.substring(XML_SCHEMA_PREFIX.length()), getClassLoader());
        }
        else
        {
            return super.getResource(location);
        }
    }
}
