/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import org.mule.raml.implv2.v08.model.RamlImpl08V2;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;

import org.raml.v2.RamlBuilder;
import org.raml.v2.impl.commons.RamlHeader;
import org.raml.v2.impl.commons.RamlVersion;
import org.raml.v2.impl.commons.model.builder.ModelBuilder;
import org.raml.v2.impl.commons.nodes.RamlDocumentNode;
import org.raml.v2.loader.ResourceLoader;
import org.raml.v2.utils.StreamUtils;

public class ParserV2Utils
{

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath)
    {
        InputStream contentStream = resourceLoader.fetchResource(ramlPath);
        if (contentStream != null)
        {
            String contentString = StreamUtils.toString(contentStream);
            return build(resourceLoader, ramlPath, contentString);
        }
        throw new RuntimeException("Invalid RAML descriptor.");
    }

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        try
        {
            RamlHeader header = RamlHeader.parse(content);
            RamlBuilder builder = new RamlBuilder();
            RamlDocumentNode ramlNode = (RamlDocumentNode) builder.build(content, resourceLoader, ramlPath);
            if (header.getVersion() == RamlVersion.RAML_10)
            {
                org.raml.v2.model.v10.api.Api ramlV2 = ModelBuilder.createRaml(org.raml.v2.model.v10.api.Api.class, ramlNode);
                return new RamlImpl10V2(ramlV2);
            }
            else
            {
                org.raml.v2.model.v08.api.Api ramlV2 = ModelBuilder.createRaml(org.raml.v2.model.v08.api.Api.class, ramlNode);
                return new RamlImpl08V2(ramlV2);
            }
        }
        catch (RamlHeader.InvalidHeaderException e)
        {
            throw new RuntimeException("Invalid RAML descriptor.");
        }
    }

}
