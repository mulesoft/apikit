/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest;

import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.AbstractWebServiceInterface;

public class RestWebServiceInterface extends AbstractWebServiceInterface
{
    public RestWebServiceInterface(String name)
    {
        super(name);
    }

    @Override
    public MessageProcessor getOperationRouter()
    {
        if (getRoutes().size() != 1)
        {
            throw new IllegalStateException("One and only one rest resource can be the root");
        }
        return new RestMessageProcessor(this);
    }

}
