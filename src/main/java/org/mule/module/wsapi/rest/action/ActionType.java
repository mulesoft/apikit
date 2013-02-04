/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest.action;

public enum ActionType
{
    CREATE("post"),
    RETRIEVE("get"),
    UPDATE("put"),
    DELETE("delete"),
    EXISTS("head");

    private String httpMethod;

    ActionType(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public static ActionType fromHttpMethod(String method)
    {
        for (ActionType op : ActionType.values())
        {
            if (op.httpMethod.equalsIgnoreCase(method))
            {
                return op;
            }
        }
        throw new IllegalArgumentException("Unable to map HTTP method to ReST operation");
    }

    public String toHttpMethod()
    {
        return httpMethod;
    }
}


