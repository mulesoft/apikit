/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.swagger;

import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.resource.StaticResourceRetrieveOperation;

import java.util.EnumSet;
import java.util.Set;

public class SwaggerConsoleResource extends AbstractRestResource
{

    protected RestOperation retrieveStaticResourceOperation;

    public SwaggerConsoleResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
        retrieveStaticResourceOperation = new StaticResourceRetrieveOperation(
            "/org/mule/module/apikit/rest/swagger");
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE);
    }

    @Override
    protected RestOperation getAction(RestOperationType actionType, RestRequest request)
        throws OperationNotAllowedException
    {
        if (RestOperationType.RETRIEVE == request.getProtocolAdaptor().getOperationType())
        {
            if (!request.hasMorePathElements()
                || request.peekNextPathElement().equalsIgnoreCase("index.html"))
            {
                return new SwaggerConsoleRetrieveOperation(request.getService());
            }

            else
            {
                return retrieveStaticResourceOperation;
            }
        }
        else
        {
            throw new OperationNotAllowedException(this, actionType);
        }
    }
}
