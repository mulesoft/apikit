/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import static org.raml.v2.internal.impl.commons.RamlVersion.RAML_08;
import static org.raml.v2.internal.impl.commons.RamlVersion.RAML_10;

import org.mule.raml.implv2.v08.model.RamlImpl08V2;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.raml.v2.internal.impl.RamlBuilder;
import org.raml.v2.internal.impl.commons.RamlHeader;
import org.raml.v2.internal.impl.commons.RamlVersion;
import org.raml.v2.internal.impl.commons.model.builder.ModelBuilder;
import org.raml.v2.internal.impl.commons.nodes.RamlDocumentNode;
import org.raml.v2.internal.impl.v10.RamlFragment;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.framework.nodes.ErrorNode;
import org.raml.v2.internal.framework.nodes.Node;
import org.raml.v2.internal.utils.StreamUtils;

public class ParserV2Utils
{

    public static final String INVALID_HEADER = "Invalid RAML header. Is not one of \"#%RAML 0.8\" or \"#%RAML 1.0\"";

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath)
    {
        return build(resourceLoader, ramlPath, readContent(resourceLoader, ramlPath));
    }

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        try
        {
            RamlHeader.parse(content);
            RamlDocumentNode ramlNode = buildTree(resourceLoader, ramlPath, content);
            return wrapTree(ramlNode);
        }
        catch (RamlHeader.InvalidHeaderException e)
        {
            throw new RuntimeException("Invalid RAML descriptor.");
        }
    }

    private static String readContent(ResourceLoader resourceLoader, String ramlPath)
    {
        InputStream contentStream = resourceLoader.fetchResource(ramlPath);
        if (contentStream != null)
        {
            return StreamUtils.toString(contentStream);
        }
        throw new RuntimeException("Invalid RAML descriptor.");
    }

    private static RamlDocumentNode buildTree(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        RamlBuilder builder = new RamlBuilder();
        return (RamlDocumentNode) builder.build(content, resourceLoader, ramlPath);
    }

    private static IRaml wrapTree(RamlDocumentNode ramlNode)
    {
        if (ramlNode.getVersion() == RamlVersion.RAML_10)
        {
            org.raml.v2.api.model.v10.api.Api ramlV2 = ModelBuilder.createRaml(org.raml.v2.api.model.v10.api.Api.class, ramlNode);
            return new RamlImpl10V2(ramlV2);
        }
        else
        {
            org.raml.v2.api.model.v08.api.Api ramlV2 = ModelBuilder.createRaml(org.raml.v2.api.model.v08.api.Api.class, ramlNode);
            return new RamlImpl08V2(ramlV2);
        }
    }

    public static List<String> validate(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        List<String> result = new ArrayList<>();
        try
        {
            RamlHeader header = RamlHeader.parse(content);
            if (header.getVersion() == RAML_08 || (header.getVersion() == RAML_10 && header.getFragment() == RamlFragment.Default))
            {
                RamlBuilder builder = new RamlBuilder();
                Node node = builder.build(content, resourceLoader, ramlPath);
                List<ErrorNode> descendantsWith = node.findDescendantsWith(ErrorNode.class);
                for (ErrorNode errorNode : descendantsWith)
                {
                    result.add(errorNode.getErrorMessage());
                }
            }
            else
            {
                result.add(INVALID_HEADER);
            }
        }
        catch (RamlHeader.InvalidHeaderException e)
        {
            result.add(INVALID_HEADER);
        }
        return result;
    }

}
