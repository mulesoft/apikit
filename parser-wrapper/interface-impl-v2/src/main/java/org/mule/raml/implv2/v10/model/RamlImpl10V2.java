/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.resources.Resource;

public class RamlImpl10V2 implements IRaml
{

    private Api api;

    public RamlImpl10V2(Api api)
    {
        this.api = api;
    }

    @Override
    public Map<String, IResource> getResources()
    {
        Map<String, IResource> map = new LinkedHashMap<>();
        List<Resource> resources = api.resources();
        for (Resource resource : resources)
        {
            map.put(resource.relativeUri().value(), new ResourceImpl(resource));
        }
        return map;
    }

    @Override
    public String getBaseUri()
    {
        return api.baseUri() != null ? api.baseUri().value() : null;
    }

    @Override
    public String getVersion()
    {
        return api.version();
    }

    @Override
    public List<Map<String, String>> getSchemas()
    {
        Map<String, String> map = new LinkedHashMap<>();
        List<TypeDeclaration> types = api.types();
        if (types.isEmpty())
        {
            types = api.schemas();
        }
        for (TypeDeclaration typeDeclaration : types)
        {
            map.put(typeDeclaration.name(), getTypeAsString(typeDeclaration));
        }
        List<Map<String, String>> result = new ArrayList<>();
        result.add(map);
        return result;
    }

    private String getTypeAsString(TypeDeclaration typeDeclaration)
    {
        String schema = typeDeclaration.schemaContent();
        if (schema != null)
        {
            return schema;
        }
        List<String> type = typeDeclaration.type();
        if (type != null && !type.isEmpty())
        {
            return type.get(0);
        }
        return null;
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
