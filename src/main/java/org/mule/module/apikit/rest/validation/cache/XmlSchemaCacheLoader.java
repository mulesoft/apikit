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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.SAXException;

public class XmlSchemaCacheLoader extends CacheLoader<String, Schema>
{

    private ResourceLoader resourceLoader;

    public XmlSchemaCacheLoader(MuleContext muleContext)
    {
        this.resourceLoader = new SchemaResourceLoader(muleContext.getExecutionClassLoader());
    }

    @Override
    public Schema load(String schemaLocation) throws IOException, SAXException
    {
        Resource schemaResource = resourceLoader.getResource(schemaLocation);
        return compileSchema(schemaResource.getInputStream());
    }

    private static Schema compileSchema(InputStream inputStream) throws SAXException
    {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        return factory.newSchema(new StreamSource(inputStream));
    }
}
