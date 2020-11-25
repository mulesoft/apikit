/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class JsonSchemaResource extends AbstractFileResolvingResource
{

    private String className;

    private ClassLoader classLoader;

    private static LoadingCache<Class<?>, byte[]> schemaCache;

    static
    {
        schemaCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(
                        new CacheLoader<Class<?>, byte[]>()
                        {
                            private ObjectMapper objectMapper;

                            public byte[] load(Class<?> clazz) throws IOException
                            {
                                if (objectMapper == null)
                                {
                                    objectMapper = JsonMapper.builder().deactivateDefaultTyping().build();
                                }

                                JsonSchema jsonSchema = this.objectMapper.generateJsonSchema(clazz);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                this.objectMapper.writeValue(baos, jsonSchema);

                                return baos.toByteArray();
                            }
                        });

    }

    public JsonSchemaResource(String className, ClassLoader classLoader)
    {
        Assert.notNull(className, "Class name must not be null");
        this.className = className;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public String getDescription()
    {
        StringBuilder builder = new StringBuilder("annotated class resource [");
        builder.append(this.className);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        try
        {
            Class<?> clazz = this.classLoader.loadClass(className);
            return new ByteArrayInputStream(schemaCache.get(clazz));
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException(getDescription() + " cannot be found", e);
        }
        catch (ExecutionException e)
        {
            throw new IOException(getDescription() + " cannot be found", e.getCause());
        }
    }
}
