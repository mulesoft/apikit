/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol.http;

import static org.mule.module.apikit.rest.operation.RestOperationType.CREATE;
import static org.mule.module.apikit.rest.operation.RestOperationType.EXISTS;
import static org.mule.module.apikit.rest.operation.RestOperationType.RETRIEVE;
import static org.mule.module.apikit.rest.operation.RestOperationType.UPDATE;

import org.mule.module.apikit.rest.operation.RestOperationType;

enum HttpMethod
{
    POST(CREATE), GET(RETRIEVE), PUT(UPDATE), DELETE(RestOperationType.DELETE), HEAD(EXISTS);

    private RestOperationType restOperationType;;

    HttpMethod(RestOperationType restOperationType)
    {
        this.restOperationType = restOperationType;
    }

    public static HttpMethod fromRestOperationType(RestOperationType operationType)
    {
        for (HttpMethod op : HttpMethod.values())
        {
            if (op.restOperationType.equals(operationType))
            {
                return op;
            }
        }
        throw new IllegalArgumentException("Unable to map HTTP method to ReST operation");
    }

    public static HttpMethod fromHttpMethodString(String method)
    {
        for (HttpMethod op : HttpMethod.values())
        {
            if (op.toString().equalsIgnoreCase(method))
            {
                return op;
            }
        }
        throw new IllegalArgumentException("Unable to map HTTP method to ReST operation");
    }

    public RestOperationType toRestOperationType()
    {
        return restOperationType;
    }

}
