/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mule.raml.implv1.model.parameter.ParameterImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.Header;

public class ResponseImpl implements IResponse
{
    Response response;
    public ResponseImpl(Response response)
    {
        this.response = response;
    }

    public Map<String, IMimeType> getBody()
    {
        if (response.getBody() == null)
        {
            return null;
        }
        Map<String, IMimeType> map = new LinkedHashMap<String, IMimeType>();
        for(Map.Entry<String, MimeType> entry : response.getBody().entrySet())
        {
            map.put(entry.getKey(), new MimeTypeImpl(entry.getValue()));
        }
        return map;
    }

    public boolean hasBody()
    {
        return response.hasBody();
    }

    public Map<String, IParameter> getHeaders()
    {
        if (response.getHeaders() == null)
        {
            return null;
        }
        Map<String, IParameter> map = new LinkedHashMap<String, IParameter>();
        for(Map.Entry<String, Header> entry : response.getHeaders().entrySet())
        {
            map.put(entry.getKey(), new ParameterImpl(entry.getValue()));
        }
        return map;
    }

    public void setBody(Map<String, IMimeType> body)
    {
        Map<String, MimeType> map = new LinkedHashMap<String, MimeType>();
        for (Map.Entry<String, IMimeType> entry : body.entrySet())
        {
            map.put(entry.getKey(), (MimeType) entry.getValue().getInstance());
        }
        response.setBody(map);
    }

    public void setHeaders(Map<String, IParameter> headers)
    {
        Map<String, Header> map = new LinkedHashMap<String, Header>();
        for (Map.Entry<String, IParameter> entry : headers.entrySet())
        {
            map.put(entry.getKey(), (Header)entry.getValue().getInstance());
        }
        response.setHeaders(map);
    }

    public Object getInstance()
    {
        return response;
    }
}
