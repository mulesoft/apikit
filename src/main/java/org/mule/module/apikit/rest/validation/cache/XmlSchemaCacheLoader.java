/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.validation.cache;

import org.mule.api.MuleContext;
import org.mule.module.apikit.rest.validation.io.SchemaResourceLoader;

import com.google.common.cache.CacheLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.raml.model.Action;
import org.raml.model.MimeType;
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
            //TODO remove hack to get schema using coords
            String[] path = schemaLocation.split(",");
            Action action = api.getResource(path[0]).getAction(path[1]);
            MimeType mimeType = action.getBody().get(path[2]);
            is = new ByteArrayInputStream(mimeType.getSchema().getBytes());
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
