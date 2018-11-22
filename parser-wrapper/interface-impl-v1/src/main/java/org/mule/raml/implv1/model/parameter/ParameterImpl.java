/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model.parameter;

import com.google.common.base.Function;
import org.mule.raml.interfaces.model.parameter.IParameter;
import org.raml.model.parameter.AbstractParam;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;

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

    public boolean isArray()
    {
        // only available in RAML 1.0+
        return false;
    }

    public boolean validate(String s)
    {
        return parameter.validate(s);
    }

    @Override
    public void validate(String expectedKey, Object values, String parameterType) throws Exception {
        Collection<?> properties;

        if (values instanceof Iterable) {
            properties = newArrayList((Iterable) values);
        }

        else properties = singletonList(values);

        if (properties.size() > 1 && !isRepeat())
        {
            throw new Exception("Parameter " + expectedKey + " is not a repetable");
        }

        Collection<String> stringProperties = transform(properties, new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return String.valueOf(input);
            }
        });

        for (String param : stringProperties)
        {
            if (!parameter.validate(param)) {
                String msg = String.format("Invalid value '%s' for %s %s. %s",
                        param, parameterType,expectedKey, message(param));
                throw new Exception(msg);
            }
        }
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

    public String getExample()
    {
        return parameter.getExample();
    }

    @Override
    public Map<String, String> getExamples()
    {
        return new HashMap<>();
    }

    public Object getInstance()
    {
        return parameter;
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
