/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.param;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParameterList implements Iterable<RestParameter>
{

    protected List<RestParameter> parameters = Collections.emptyList();

    public ParameterList()
    {
    }

    public ParameterList(RestParameter... parameter)
    {
        parameters = Arrays.asList(parameter);
    }

    @Override
    public Iterator<RestParameter> iterator()
    {
        return parameters.iterator();
    }

    public List<RestParameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<RestParameter> parameters)
    {
        this.parameters = parameters;
    }

}
