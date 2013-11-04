/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.api.MuleContext;
import org.mule.module.apikit.validation.io.SchemaResourceLoader;

import com.google.common.cache.CacheLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.raml.model.Raml;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.SAXException;

public class XmlSchemaCacheLoader extends CacheLoader<String, Schema>
{

    private ResourceLoader resourceLoader;
    private Raml api;

    public XmlSchemaCacheLoader(MuleContext muleContext, Raml api)
    {
        this.api = api;
        this.resourceLoader = new SchemaResourceLoader(muleContext.getExecutionClassLoader());
    }

    @Override
    public Schema load(String schemaLocation) throws IOException, SAXException
    {
        InputStream is;

        if (schemaLocation.startsWith("/"))
        {
            //inline schema definition
            is = new ByteArrayInputStream(SchemaCacheUtils.resolveSchema(schemaLocation, api).getBytes());
        }
        else
        {
            Resource schemaResource = resourceLoader.getResource(schemaLocation);
            is = schemaResource.getInputStream();
        }
        return compileSchema(is);
    }

    private static Schema compileSchema(InputStream inputStream) throws SAXException
    {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        return factory.newSchema(new StreamSource(inputStream));
    }
}
