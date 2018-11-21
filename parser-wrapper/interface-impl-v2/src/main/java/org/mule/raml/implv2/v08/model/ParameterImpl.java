/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v08.model;

import org.mule.raml.interfaces.model.parameter.IParameter;

import org.raml.v2.api.model.v08.parameters.Parameter;

import java.util.HashMap;
import java.util.Map;

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
        return parameter.required();
    }

    @Override
    public String getDefaultValue()
    {
        if (parameter.defaultValue() == null)
        {
            return null;
        }
        return parameter.defaultValue().toString();
    }

    @Override
    public boolean isRepeat()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isArray()
    {
        // only available in RAML 1.0+
        return false;
    }

    @Override
    public boolean validate(String value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(String expectedKey, Object values, String parameterType) throws Exception {
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
    public String getExample()
    {
        return parameter.example();
    }

    @Override
    public Map<String, String> getExamples()
    {
        // only available in RAML 1.0+
        return new HashMap<>();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStringArray() {
        return false;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public boolean isFacetArray(String facet) {
        return false;
    }
}
