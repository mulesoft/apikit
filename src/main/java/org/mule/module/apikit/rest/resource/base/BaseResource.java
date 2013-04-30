/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.base;

import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;

import java.util.EnumSet;
import java.util.Set;

public class BaseResource extends AbstractHierarchicalRestResource
{

    public BaseResource()
    {
        super("", null);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS);
    }

    private RestOperation getSwaggerOperation()
    {
        if (swaggerOperation == null)
        {
            SwaggerResourceDescriptorOperation op = new SwaggerResourceDescriptorOperation();
            op.setResource(this);
            swaggerOperation = op;
        }
        return swaggerOperation;
    }

}
