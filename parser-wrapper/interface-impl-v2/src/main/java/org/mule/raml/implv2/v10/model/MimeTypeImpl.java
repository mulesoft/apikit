/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.List;
import java.util.Map;

import org.raml.v2.model.v10.datamodel.TypeDeclaration;

public class MimeTypeImpl implements IMimeType
{

    private TypeDeclaration typeDeclaration;

    public MimeTypeImpl(TypeDeclaration typeDeclaration)
    {
        this.typeDeclaration = typeDeclaration;
    }

    @Override
    public String getType()
    {
        return typeDeclaration.name();
    }

    @Override
    public Object getCompiledSchema()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchema()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<IParameter>> getFormParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExample()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }
}
