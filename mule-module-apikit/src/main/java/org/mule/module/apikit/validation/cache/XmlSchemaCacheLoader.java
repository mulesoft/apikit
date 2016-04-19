/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.raml.interfaces.model.IRaml;

import com.google.common.cache.CacheLoader;

import java.io.IOException;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

public class XmlSchemaCacheLoader extends CacheLoader<String, Schema>
{

    private IRaml api;

    public XmlSchemaCacheLoader(IRaml api)
    {
        this.api = api;
    }

    @Override
    public Schema load(String schemaLocation) throws IOException, SAXException
    {
        return SchemaCacheUtils.resolveXmlSchema(schemaLocation, api);
    }

}
