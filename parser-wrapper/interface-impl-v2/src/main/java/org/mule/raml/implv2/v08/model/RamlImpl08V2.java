/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import com.google.common.base.Optional;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.api.GlobalSchema;
import org.raml.v2.api.model.v08.resources.Resource;

import static org.mule.raml.implv2.ParserV2Utils.nullSafe;

public class RamlImpl08V2 implements IRaml
{
    private Api api;
    private Optional<String> version;

    public RamlImpl08V2(Api api)
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
    public String getVersion()
    {
        if (version == null) {
            version = Optional.fromNullable(api.version());
        }
        return version.orNull();
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
        Map<String, String> map = new LinkedHashMap<>();
        List<GlobalSchema> schemas = api.schemas();
        for (GlobalSchema schema : schemas)
        {
            map.put(schema.key(), schema.value() != null ? schema.value().value() : null);
        }
        List<Map<String, String>> result = new ArrayList<>();
        result.add(map);
        return result;
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

    @Override
    public List<String> getAllReferences() {
        return Collections.emptyList();
    }
}
