/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

public class RestException extends Exception
{

    private static final long serialVersionUID = -8161118644314571187L;

    public RestException()
    {
        super();
    }

    public RestException(Exception cause)
    {
        super(cause);
    }

}
