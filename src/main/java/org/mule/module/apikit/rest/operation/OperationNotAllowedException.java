/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.operation;

import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.resource.RestResource;

public class OperationNotAllowedException extends RestException
{

    private static final long serialVersionUID = 7820998020825499825L;

    protected RestResource resource;
    protected RestOperationType actionType;

    public OperationNotAllowedException(RestResource resource, RestOperationType actionType)
    {
        this.resource = resource;
        this.actionType = actionType;
    }

    public RestResource getResource()
    {
        return resource;
    }

}
