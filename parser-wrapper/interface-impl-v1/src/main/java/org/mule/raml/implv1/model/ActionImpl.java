/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import org.mule.raml.implv1.model.parameter.ParameterImpl;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IQueryString;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.ISecurityReference;
import org.mule.raml.interfaces.model.parameter.IParameter;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.SecurityReference;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActionImpl implements IAction
{
    Action action;
    public ActionImpl(Action action)
    {
        this.action = action;
    }

    public IActionType getType()
    {
        return IActionType.valueOf(action.getType().name());
    }

    public IResource getResource()
    {
        Resource resource = action.getResource();
        if (resource == null)
        {
            return null;
        }
        return new ResourceImpl(resource);

    }

    public Map<String, IMimeType> getBody()
    {
        if (action.getBody() == null)
        {
            return null;
        }
        Map<String, IMimeType> map = new CaseInsensitiveMap();
        for(Map.Entry<String, MimeType> entry : action.getBody().entrySet())
        {
            map.put(entry.getKey(), new MimeTypeImpl(entry.getValue()));
        }
        return map;
    }

    public Map<String, List<IParameter>> getBaseUriParameters()
    {
        if (action.getBaseUriParameters() == null)
        {
            return null;
        }
        Map<String, List<IParameter>> map = new LinkedHashMap<String, List<IParameter>>();
        for (Map.Entry<String, List<UriParameter>> entry : action.getBaseUriParameters().entrySet())
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

    public Map<String, IParameter> getQueryParameters()
    {
        if (action.getQueryParameters() == null)
        {
            return null;
        }
        Map<String, IParameter> map = new LinkedHashMap<String, IParameter>();
        for (Map.Entry<String, QueryParameter> entry : action.getQueryParameters().entrySet())
        {
            map.put(entry.getKey(),new ParameterImpl(entry.getValue()));
        }
        return map;
    }

    @Override
    public IParameter getQueryString() {
        return null;
    }

    @Override
    public IQueryString queryString() {
        return null;
    }


    public boolean hasBody()
    {
        return action.hasBody();
    }

    public Map<String, IResponse> getResponses()
    {
        if (action.getResponses() == null)
        {
            return null;
        }
        Map<String, IResponse> map = new LinkedHashMap<String, IResponse>();
        for(Map.Entry<String, Response> entry : action.getResponses().entrySet())
        {
            map.put(entry.getKey(), new ResponseImpl(entry.getValue()));
        }
        return map;
    }

    public Map<String, IParameter> getHeaders()
    {
        if (action.getHeaders() == null)
        {
            return null;
        }
        Map<String, IParameter> map = new LinkedHashMap<String, IParameter>();
        for(Map.Entry<String, Header> entry : action.getHeaders().entrySet())
        {
            map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
        }
        return map;
    }

    public List<ISecurityReference> getSecuredBy()
    {
        if (action.getSecuredBy() == null)
        {
            return null;
        }
        List<ISecurityReference> list = new ArrayList<ISecurityReference>();
        for(SecurityReference securityReference : action.getSecuredBy())
        {
            list.add(new SecurityReferenceImpl(securityReference));
        }
        return list;
    }

    public List<String> getIs()
    {
        return action.getIs();
    }

    public void cleanBaseUriParameters()
    {
        action.getBaseUriParameters().clear();
    }

    public void setHeaders(Map<String, IParameter> headers)
    {
        Map<String, Header> map = new LinkedHashMap<String, Header>();
        for (Map.Entry<String, IParameter> entry : headers.entrySet())
        {
            map.put(entry.getKey(), (Header)entry.getValue().getInstance());
        }
        action.setHeaders(map);
    }

    public void setQueryParameters(Map<String, IParameter> queryParameters)
    {
        Map<String, QueryParameter> map = new LinkedHashMap<String, QueryParameter>();
        for (Map.Entry<String, IParameter> entry : queryParameters.entrySet())
        {
            map.put(entry.getKey(), (QueryParameter) entry.getValue().getInstance());
        }
        action.setQueryParameters(map);
    }

    public void setBody(Map<String, IMimeType> body)
    {
        Map<String, MimeType> map = new LinkedHashMap<String, MimeType>();
        for (Map.Entry<String, IMimeType> entry : body.entrySet())
        {
            map.put(entry.getKey(), (MimeType) entry.getValue().getInstance());
        }
        action.setBody(map);
    }

    public void addResponse(String s, IResponse iResponse)
    {
        action.getResponses().put(s, (Response)iResponse.getInstance());
    }

    public void addSecurityReference(String securityReferenceName)
    {
        action.getSecuredBy().add(new SecurityReference(securityReferenceName));
    }

    public void addIs(String s)
    {
        action.getIs().add(s);
    }

}
