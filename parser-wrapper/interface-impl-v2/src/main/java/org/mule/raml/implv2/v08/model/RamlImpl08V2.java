/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

import org.raml.v2.model.v08.api.Api;

public class RamlImpl08V2 implements IRaml
{
    private Api api;

    public RamlImpl08V2(Api api)
    {
        this.api = api;
    }

    @Override
    public IResource getResource(String path)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getConsolidatedSchemas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getCompiledSchemas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBaseUri()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, IResource> getResources()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, IParameter> getBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, ISecurityScheme>> getSecuritySchemes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, ITemplate>> getTraits()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUri()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> getSchemas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void injectTrait(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme)
    {
        throw new UnsupportedOperationException();
    }
}
