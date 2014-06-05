/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.injector;

import org.raml.model.Action;
import org.raml.model.SecurityReference;
import org.raml.model.SecurityScheme;

public class InjectableSecurityScheme extends InjectableRamlFeature
{

    private SecurityScheme cache;
    private static final String KEY = "scheme";

    private static final String TEMPLATE = "#%RAML 0.8\ntitle: t\nsecuritySchemes:\n - " + KEY + ":\n";
    private static final String INDENTATION = "    ";


    public InjectableSecurityScheme(String name, String securitySchemeYaml)
    {
        super(name, securitySchemeYaml);
        cache = parse().getSecuritySchemes().get(0).get(KEY);
    }

    public SecurityScheme getSecurityScheme()
    {
        return cache;
    }

    @Override
    public void applyToAction(Action target)
    {
        target.getSecuredBy().add(new SecurityReference(name));
    }

    @Override
    protected String getBoilerPlateBefore()
    {
        return TEMPLATE;
    }

    @Override
    protected String getBoilerPlateAfter()
    {
        return "";
    }

    @Override
    protected String getIndentation()
    {
        return INDENTATION;
    }

}
