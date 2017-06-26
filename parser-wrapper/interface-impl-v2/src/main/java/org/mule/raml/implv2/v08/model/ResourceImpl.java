/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import static org.mule.raml.interfaces.ParserUtils.resolveVersion;

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
    private Map<IActionType, IAction> actions;
    private Map<String, IParameter> resolvedUriParameters;

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
    public String getResolvedUri(String version)
    {
        return resolveVersion(getUri(), version);
    }

    @Override
    public String getParentUri()
    {
        return getUri().substring(0, getUri().length() - getRelativeUri().length());
    }

    @Override
    public IAction getAction(String name)
    {
        return getActions().get(getActionKey(name));
    }

    @Override
    public Map<IActionType, IAction> getActions()
    {
        if (actions == null) {
            actions = loadActions(resource);
        }
        return actions;
    }

    private static Map<IActionType, IAction> loadActions(Resource resource) {
        Map<IActionType, IAction> map = new LinkedHashMap<>();
        for (Method method : resource.methods())
        {
            map.put(getActionKey(method.method()), new ActionImpl(method));
        }
        return map;
    }

    private static IActionType getActionKey(String method) {
        return IActionType.valueOf(method.toUpperCase());
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
        if (resolvedUriParameters == null)
        {
            resolvedUriParameters = loadResolvedUriParameters(resource);
        }

        return resolvedUriParameters;
    }

    private static Map<String, IParameter> loadResolvedUriParameters(Resource resource) {
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

    @Override
    public String toString()
    {
        return getUri();
    }
}
