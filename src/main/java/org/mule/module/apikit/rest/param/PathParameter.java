/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.param;

import org.mule.module.apikit.rest.RestParameter;

public class PathParameter extends RestParameter
{

    private boolean allowMultiple;

    public PathParameter(String name)
    {
        this.name = name;
    }

    public boolean isAllowMultiple()
    {
        return allowMultiple;
    }

}
