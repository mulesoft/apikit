/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.param;

import java.util.List;

public class RestParameter
{

    protected String name;
    protected String description;
    protected String defaultValue;
    protected boolean required;
    protected boolean allowMultiple;
    protected List<String> allowableValues;


    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isAllowMultiple()
    {
        return allowMultiple;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public List<String> getAllowableValues()
    {
        return allowableValues;
    }

    public void setAllowableValues(List<String> allowableValues)
    {
        this.allowableValues = allowableValues;
    }

}
