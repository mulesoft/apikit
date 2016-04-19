/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mule.raml.implv1.model.parameter.ParameterImpl;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.MimeType;
import org.raml.model.parameter.FormParameter;

public class MimeTypeImpl implements IMimeType
{
    MimeType mimeType;
    public MimeTypeImpl(MimeType mimeType)
    {
        this.mimeType = mimeType;
    }

    public MimeTypeImpl(String type)
    {
        mimeType = new MimeType(type);
    }

    public Object getCompiledSchema()
    {
        return mimeType.getCompiledSchema();
    }

    public String getSchema()
    {
        return mimeType.getSchema();
    }

    public Map<String, List<IParameter>> getFormParameters()
    {
        if (mimeType.getFormParameters() == null)
        {
            return null;
        }
        Map<String, List<IParameter>> map = new LinkedHashMap<String, List<IParameter>>();
        for (Map.Entry<String, List<FormParameter>> entry : mimeType.getFormParameters().entrySet())
        {
            List<IParameter> list = new ArrayList<IParameter>();
            for (FormParameter formparameter : entry.getValue())
            {
                list.add(new ParameterImpl(formparameter));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public String getType()
    {
        return mimeType.getType();
    }

    public String getExample()
    {
        return mimeType.getExample();
    }

    public MimeType getInstance()
    {
        return mimeType;
    }
}
