/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

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
    public String getExample()
    {
        ExampleSpec example = typeDeclaration.example();
        if (example != null && example.value() != null)
        {
            return example.value();
        }
        List<ExampleSpec> examples = typeDeclaration.examples();
        if (examples != null && !examples.isEmpty())
        {
            if (examples.get(0).value() != null)
            {
                return examples.get(0).value();
            }
        }
        return null;
    }

    @Override
    public String getSchema()
    {
        String schema = typeDeclaration.schema();
        if (schema != null)
        {
            return schema;
        }
        List<String> type = typeDeclaration.type();
        if (type != null && !type.isEmpty())
        {
            return type.get(0);
        }
        return null;
    }

    @Override
    public Map<String, List<IParameter>> getFormParameters()
    {
        // no longer supported in RAML 1.0
        return new HashMap<>();
    }

    public List<ValidationResult> validate(String payload)
    {
        return typeDeclaration.validate(payload);
    }

    @Override
    public Object getCompiledSchema()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }
}
