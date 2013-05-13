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
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.param.PathParameter;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;

import java.util.EnumSet;
import java.util.Set;

public class CollectionMemberResource extends AbstractHierarchicalRestResource
{

    public CollectionMemberResource(RestResource parentResource)
    {
        super(parentResource.getName() + "Member", parentResource);
        parameters.add(new PathParameter(parentResource.getName() + "MemberId"));
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE,
            RestOperationType.DELETE, RestOperationType.OPTIONS);
    }

    @Override
    public void handle(RestRequest restRequest) throws RestException
    {
        restRequest.getMuleEvent().setFlowVariable(getName() + "Id", restRequest.getNextPathElement());
        super.handle(restRequest);
    }

    @Override
    public String getPath()
    {
        return parentResource.getPath() + "/{" + getName() + "Id}";
    }

    @Override
    public String getDescription()
    {
        if (super.getDescription() != null)
        {
            return super.getDescription();
        }
        else
        {
            return getName() + " Collection Member";
        }
    }

}
