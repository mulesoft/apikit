/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.IResponse;
import org.mule.raml.interfaces.model.ISecurityReference;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;

public class ActionImpl implements IAction
{

    private Method method;

    public ActionImpl(Method method)
    {
        this.method = method;
    }

    @Override
    public IActionType getType()
    {
        return IActionType.valueOf(method.method().toUpperCase());
    }

    @Override
    public boolean hasBody()
    {
        return !method.body().isEmpty();
    }

    @Override
    public Map<String, IResponse> getResponses()
    {
        Map<String, IResponse> result = new LinkedHashMap<>();
        for (Response response : method.responses())
        {
            result.put(response.code().value(), new ResponseImpl(response));
        }
        return result;
    }

    @Override
    public IResource getResource()
    {
        return new ResourceImpl(method.resource());
    }

    @Override
    public Map<String, IMimeType> getBody()
    {
        Map<String, IMimeType> result = new LinkedHashMap<>();
        for (TypeDeclaration typeDeclaration : method.body())
        {
            result.put(typeDeclaration.name(),  new MimeTypeImpl(typeDeclaration));
        }
        return result;
    }

    @Override
    public Map<String, IParameter> getQueryParameters()
    {
        Map<String, IParameter> result = new HashMap<>();
        for (TypeDeclaration typeDeclaration : method.queryParameters())
        {
            result.put(typeDeclaration.name(), new ParameterImpl(typeDeclaration));
        }
        return result;
    }

    @Override
    public Map<String, List<IParameter>> getBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, IParameter> getHeaders()
    {
        Map<String, IParameter> result = new HashMap<>();
        for (TypeDeclaration typeDeclaration : method.headers())
        {
            result.put(typeDeclaration.name(), new ParameterImpl(typeDeclaration));
        }
        return result;
    }

    @Override
    public List<ISecurityReference> getSecuredBy()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getIs()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeaders(Map<String, IParameter> headers)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setQueryParameters(Map<String, IParameter> queryParameters)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBody(Map<String, IMimeType> body)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResponse(String key, IResponse response)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSecurityReference(String securityReferenceName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIs(String is)
    {
        throw new UnsupportedOperationException();
    }
}
