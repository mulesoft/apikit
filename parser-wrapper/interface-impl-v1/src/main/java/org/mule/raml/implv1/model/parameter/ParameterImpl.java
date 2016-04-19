/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model.parameter;

import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.parameter.AbstractParam;

public class ParameterImpl implements IParameter
{

    AbstractParam parameter;

    public ParameterImpl(AbstractParam parameter)
    {
        this.parameter = parameter;
    }

    public boolean isRequired()
    {
        return parameter.isRequired();
    }

    public String getDefaultValue()
    {
        return parameter.getDefaultValue();
    }

    public boolean isRepeat()
    {
        return parameter.isRepeat();
    }

    public boolean validate(String s)
    {
        return parameter.validate(s);
    }

    public String message(String s)
    {
        return parameter.message(s);
    }

    public String getDisplayName()
    {
        return parameter.getDisplayName();
    }

    public String getDescription()
    {
        return parameter.getDescription();
    }

    public Object getInstance()
    {
        return parameter;
    }


}
