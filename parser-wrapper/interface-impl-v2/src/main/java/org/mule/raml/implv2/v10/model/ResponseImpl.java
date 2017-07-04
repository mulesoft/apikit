/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

public class ResponseImpl implements IResponse
{

    private Response response;
    private Map<String, IMimeType> body;

    public ResponseImpl(Response response)
    {
        this.response = response;
    }

    @Override
    public boolean hasBody()
    {
        return !getBody().isEmpty();
    }

    @Override
    public Map<String, IMimeType> getBody()
    {
        if (body == null)
        {
            body = loadBody(response);
        }

        return body;
    }

    private static Map<String, IMimeType> loadBody(Response response) {
        Map<String, IMimeType> result = new LinkedHashMap<>();
        for (TypeDeclaration typeDeclaration : response.body())
        {
            result.put(typeDeclaration.name(), new MimeTypeImpl(typeDeclaration));
        }
        return result;
    }

    @Override
    public Map<String, IParameter> getHeaders()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBody(Map<String, IMimeType> body)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeaders(Map<String, IParameter> headers)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }
}
