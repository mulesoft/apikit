/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.parameters.Parameter;
import org.raml.v2.api.model.v08.resources.Resource;

public class ResourceImpl implements IResource
{

    private Resource resource;

    public ResourceImpl(Resource resource)
    {
        this.resource = resource;
    }

    @Override
    public String getRelativeUri()
    {
        return resource.relativeUri().value();
    }

    @Override
    public String getUri()
    {
        return resource.resourcePath();
    }

    @Override
    public String getParentUri()
    {
        return getUri().substring(0, getUri().length() - getRelativeUri().length());
    }

    @Override
    public IAction getAction(String name)
    {
        for (Method method : resource.methods())
        {
            if (method.method().equals(name))
            {
                return new ActionImpl(method);
            }
        }
        return null;
    }

    @Override
    public Map<IActionType, IAction> getActions()
    {
        Map<IActionType, IAction> map = new LinkedHashMap<>();
        for (Method method : resource.methods())
        {
            map.put(IActionType.valueOf(method.method().toUpperCase()), new ActionImpl(method));
        }
        return map;
    }

    @Override
    public Map<String, IResource> getResources()
    {
        Map<String, IResource> result = new HashMap<>();
        for (Resource item : resource.resources())
        {
            result.put(item.relativeUri().value(), new ResourceImpl(item));
        }
        return result;
    }

    @Override
    public String getDisplayName()
    {
        return resource.displayName();
    }

    @Override
    public Map<String, IParameter> getResolvedUriParameters()
    {
        Map<String, IParameter> result = new HashMap<>();
        Resource current = resource;
        while (current != null)
        {
            for (Parameter parameter : current.uriParameters())
            {
                result.put(parameter.name(), new ParameterImpl(parameter));
            }
            current = current.parentResource();
        }
        return result;
    }

    @Override
    public void setParentUri(String parentUri)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<IParameter>> getBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }
}
