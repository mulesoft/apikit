/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractRestResource;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.util.StringUtils;

import java.util.EnumSet;
import java.util.Set;

public class CollectionResource extends AbstractRestResource
{

    protected CollectionMemberResource memberResource;

    public CollectionResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.CREATE, RestOperationType.OPTIONS);
    }

    public void setMemberResource(CollectionMemberResource memberResource)
    {
        this.memberResource = memberResource;
    }

    public CollectionMemberResource getMemberResource()
    {
        return memberResource;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        if (request.hasMorePathElements())
        {
            memberResource.handle(request);
        }
        else
        {
            processResource(request);
        }
    }

    @Override
    protected RestOperation getAction(RestOperationType actionType, RestRequest request)
        throws OperationNotAllowedException
    {
        if (actionType == RestOperationType.CREATE)
        {
            return memberResource.getOperations().get(0);
        }
        else
        {
            return super.getAction(actionType, request);
        }
    }

    @Override
    public String getDescription()
    {
        if (!StringUtils.isEmpty(super.getDescription()))
        {
            return super.getDescription();
        }
        else
        {
            return getName() + " Collection";
        }
    }
}
