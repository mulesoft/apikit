/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.raml;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.metadata.interfaces.Parseable;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RamlHandler
{
    public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

    private ResourceLoader resourceLoader;

    public RamlHandler(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public IRaml getRamlApi(String uri) {

        try
        {
            File ramlFile = resourceLoader.getRamlResource(uri);
            String ramlContent = getRamlContent(ramlFile);
            Parseable parser = getParser(ramlContent);
            return parser.build(ramlFile, ramlContent);

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private Parseable getParser(String ramlContent)
    {
        if (useParserV2(ramlContent)) {
            return new RamlV2Parser();
        } else {
            return new RamlV1Parser();
        }
    }

    private String getRamlContent(File uri) throws FileNotFoundException
    {
        try
        {
            return IOUtils.toString(new FileInputStream(uri));

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean useParserV2(String content)
    {
        String property = System.getProperty(PARSER_V2_PROPERTY);
        if (property != null && Boolean.valueOf(property))
        {
            return true;
        }
        else
        {
            return content.startsWith("#%RAML 1.0");
        }
    }
}
