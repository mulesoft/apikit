/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mule.raml.implv1.ParserV1Utils;
import org.mule.raml.implv1.model.parameter.ParameterImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.SecurityScheme;
import org.raml.model.Template;
import org.raml.model.parameter.UriParameter;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;

public class RamlImplV1 implements IRaml
{
    private final Logger logger;
    private Raml raml;
    private String ramlPath;
    private ResourceLoader resourceLoader;

    public RamlImplV1(Raml raml){
        this.raml = raml;
        this.logger = LoggerFactory.getLogger(RamlImplV1.class);
    }

    public RamlImplV1(Raml raml, String ramlPath, ResourceLoader resourceLoader)
    {
        this.raml = raml;
        this.ramlPath = ramlPath;
        this.resourceLoader = resourceLoader;
        this.logger = LoggerFactory.getLogger(RamlImplV1.class);
    }

    public Raml getRaml()
    {
        return raml;
    }

    public IResource getResource(String s)
    {
        Resource resource = raml.getResource(s);
        if (resource == null)
        {
            return null;
        }
        return new ResourceImpl(resource);
    }

    public Map<String, String> getConsolidatedSchemas()
    {
        return raml.getConsolidatedSchemas();
    }

    public Map<String, Object> getCompiledSchemas()
    {
        return raml.getCompiledSchemas();
    }

    public String getBaseUri()
    {
        return raml.getBaseUri();
    }

    public Map<String, IResource> getResources()
    {
        if (raml.getResources() == null)
        {
            return null;
        }
        Map<String, IResource> map = new LinkedHashMap<String, IResource>();
        for (Map.Entry<String, Resource> entry : raml.getResources().entrySet())
        {
            map.put(entry.getKey(),new ResourceImpl(entry.getValue()));
        }
        return map;
    }

    public String getVersion()
    {
        return raml.getVersion();
    }

    public void setBaseUri(String s)
    {
        raml.setBaseUri(s);
    }

    public Map<String, IParameter> getBaseUriParameters()
    {
        if (raml.getBaseUriParameters() == null)
        {
            return null;
        }
        Map<String, IParameter> map = new LinkedHashMap<String, IParameter>();
        for (Map.Entry<String, UriParameter> entry : raml.getBaseUriParameters().entrySet())
        {
            map.put(entry.getKey(),new ParameterImpl(entry.getValue()));
        }
        return map;
    }

    //public void setCompiledSchemas(Map<String, Object> map)
    //{
    //    raml.setCompiledSchemas(map);
    //}

    public List<Map<String, ISecurityScheme>> getSecuritySchemes()
    {
        if (raml.getSecuritySchemes() == null)
        {
            return null;
        }
        List<Map<String, ISecurityScheme>> list = new ArrayList<Map<String, ISecurityScheme>>();
        for (Map<String, SecurityScheme> map : raml.getSecuritySchemes())
        {
            Map<String, ISecurityScheme> newMap = new LinkedHashMap<String, ISecurityScheme>();
            for (Map.Entry<String, SecurityScheme> entry : map.entrySet())
            {
                newMap.put(entry.getKey(), new SecuritySchemeImpl(entry.getValue()));
            }
            list.add(newMap);
        }
        return list;
    }

    public List<Map<String, ITemplate>> getTraits()
    {
        if (raml.getTraits() == null)
        {
            return null;
        }
        List<Map<String, ITemplate>> list = new ArrayList<Map<String, ITemplate>>();
        for (Map<String, Template> map : raml.getTraits())
        {
            Map<String, ITemplate> newMap = new LinkedHashMap<String, ITemplate>();
            for (Map.Entry<String, Template> entry : map.entrySet())
            {
                newMap.put(entry.getKey(), new TemplateImpl(entry.getValue()));
            }
            list.add(newMap);
        }
        return list;
    }

    public String getUri()
    {
        return raml.getUri();
    }

    public List<Map<String, String>> getSchemas()
    {
        return raml.getSchemas();
    }

    public Object getInstance()
    {
        return raml;
    }

    public void cleanBaseUriParameters()
    {
        raml.getBaseUriParameters().clear();
    }

    public void injectTrait(String name)
    {
        Map<String, ITemplate> traitDef = new HashMap<String, ITemplate>();
        Template template = new Template();
        template.setDisplayName(name);
        //ITemplate iTemplate = new TemplateImpl(template);
        //traitDef.put(name, iTemplate);
        Map<String, Template> map = new HashMap<String, Template>();
        for(Map.Entry<String, ITemplate> entry : traitDef.entrySet())
        {
            map.put(entry.getKey(), template);
        }
        raml.getTraits().add(map);
    }

    public void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme)
    {
        Map<String, SecurityScheme> map = new HashMap<String, SecurityScheme>();
        for(Map.Entry<String, ISecurityScheme> entry : securityScheme.entrySet())
        {
            map.put(entry.getKey(), (SecurityScheme)entry.getValue().getInstance());
        }
        raml.getSecuritySchemes().add(map);
    }

    @Override
    public List<String> getAllReferences() {
        try {
            return ParserV1Utils.detectIncludes(ramlPath.replace("/", File.separator), resourceLoader);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return emptyList();
    }

    public String getRamlPath() {
        return ramlPath;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
