/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import org.raml.v2.api.model.v08.parameters.Parameter;

public class ParameterImpl implements IParameter
{

    private Parameter parameter;

    public ParameterImpl(Parameter parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public boolean isRequired()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultValue()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRepeat()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validate(String value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String message(String value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }
}
