/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import static org.mule.raml.interfaces.ParserUtils.resolveVersion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mule.raml.implv1.model.parameter.ParameterImpl;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;

public class ResourceImpl implements IResource
{
    Resource resource;

    public ResourceImpl (Resource resource)
    {
        this.resource = resource;
    }

    public IAction getAction(String s)
    {
        Action action = resource.getAction(s);
        if (action == null)
        {
            return null;
        }
        return new ActionImpl(action);

    }

    @Override
    public String getUri()
    {
        return resource.getUri();
    }

    @Override
    public String getResolvedUri(String version)
    {
        return resolveVersion(getUri(), version);
    }

    public void setParentUri(String s)
    {
        resource.setParentUri(s);
    }

    public Map<String, IResource> getResources()
    {
        if (resource.getResources() == null)
        {
            return null;
        }
        Map<String, IResource> map = new LinkedHashMap<String, IResource>();
        for(Map.Entry<String, Resource> entry : resource.getResources().entrySet())
        {
            map.put(entry.getKey(), new ResourceImpl(entry.getValue()));
        }
        return map;
    }

    public String getParentUri()
    {
        return resource.getParentUri();
    }

    public Map<IActionType, IAction> getActions()
    {
        if (resource.getActions() == null)
        {
            return null;
        }
        Map<IActionType, IAction> map = new LinkedHashMap<IActionType, IAction>();
        for(Map.Entry<ActionType, Action> entry : resource.getActions().entrySet())
        {
            map.put(IActionType.valueOf(entry.getKey().name()), new ActionImpl(entry.getValue()));
        }
        return map;
    }

    public Map<String, List<IParameter>> getBaseUriParameters()
    {
        if (resource.getBaseUriParameters() == null)
        {
            return null;
        }
        Map<String, List<IParameter>> map = new LinkedHashMap<String, List<IParameter>>();
        for (Map.Entry<String, List<UriParameter>> entry : resource.getBaseUriParameters().entrySet())
        {
            List<IParameter> list = new ArrayList<IParameter>();
            for (UriParameter parameter : entry.getValue())
            {
                list.add(new ParameterImpl(parameter));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public Map<String, IParameter> getResolvedUriParameters()
    {
        if (resource.getResolvedUriParameters() == null)
        {
            return null;
        }
        Map<String, IParameter> map = new LinkedHashMap<String, IParameter>();
        for(Map.Entry<String, UriParameter> entry : resource.getResolvedUriParameters().entrySet())
        {
            map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
        }
        return map;
    }

    public String getDisplayName()
    {
        return resource.getDisplayName();
    }

    public String getRelativeUri()
    {
        return resource.getRelativeUri();
    }

    public void cleanBaseUriParameters()
    {
        resource.getBaseUriParameters().clear();
    }

    @Override
    public String toString()
    {
        return getUri();
    }
}
