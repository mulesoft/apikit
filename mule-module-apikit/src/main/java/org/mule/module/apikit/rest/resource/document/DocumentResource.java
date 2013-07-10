/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.resource.document;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;
import org.mule.module.apikit.rest.resource.RestResource;

import java.util.EnumSet;
import java.util.Set;

public class DocumentResource extends AbstractHierarchicalRestResource
{
    public DocumentResource(String name, RestResource parentResource)
    {
        super(name, parentResource);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.UPDATE, RestOperationType.OPTIONS);
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
            return getName() + " Document";
        }
    }

}
