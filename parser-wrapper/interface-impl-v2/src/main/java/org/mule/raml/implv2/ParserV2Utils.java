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

import java.util.ArrayList;
import java.util.List;

import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.system.types.AnnotableSimpleType;

public class ParserV2Utils
{
    public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath)
    {
        RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
        return wrapApiModel(ramlModelResult, resourceLoader,  ramlPath);
    }

    public static IRaml build(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(content, ramlPath);
        return wrapApiModel(ramlModelResult, resourceLoader,  ramlPath);
    }

    private static IRaml wrapApiModel(RamlModelResult ramlModelResult,ResourceLoader resourceLoader, String ramlPath)
    {
        if (ramlModelResult.hasErrors())
        {
            throw new RuntimeException("Invalid RAML descriptor.");
        }
        if (ramlModelResult.isVersion08())
        {
            return new RamlImpl08V2(ramlModelResult.getApiV08());
        }
        return new RamlImpl10V2(ramlModelResult.getApiV10(), resourceLoader,  ramlPath);
    }

    public static List<String> validate(ResourceLoader resourceLoader, String ramlPath, String content)
    {
        List<String> result = new ArrayList<>();

        try
        {
            RamlModelResult ramlApiResult = new RamlModelBuilder(resourceLoader).buildApi(content, ramlPath);
            for (ValidationResult validationResult : ramlApiResult.getValidationResults())
            {
                result.add(validationResult.toString());
            }
        }
        catch (Exception e)
        {
            result.add("Raml parser uncaught exception: " + e.getMessage());
        }
        return result;
    }

    public static List<String> validate(ResourceLoader resourceLoader, String ramlPath)
    {
        return validate(resourceLoader, ramlPath, null);
    }

    public static boolean useParserV2(String content)
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

    public static String nullSafe(AnnotableSimpleType<?> simpleType)
    {
        return simpleType != null ? String.valueOf(simpleType.value()) : null;
    }

}
