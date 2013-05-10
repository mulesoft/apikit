/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.swagger;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.StaticResourceCollection;

import java.util.EnumSet;
import java.util.Set;

public class SwaggerConsoleResource extends AbstractRestResource
{

    protected StaticResourceCollection staticResourceCollection;;

    public SwaggerConsoleResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
        staticResourceCollection = new StaticResourceCollection(getName(), parentResource,
            "/org/mule/module/apikit/rest/swagger");
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE);
    }

}
