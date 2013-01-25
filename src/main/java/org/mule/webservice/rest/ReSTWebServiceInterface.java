/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.rest;

import org.mule.api.processor.MessageRouter;
import org.mule.webservice.AbstractWebServiceInterface;

public class ReSTWebServiceInterface extends AbstractWebServiceInterface
{

    public ReSTWebServiceInterface(String name)
    {
        super(name);
    }

    @Override
    public MessageRouter getOperationRouter()
    {
        // TODO Auto-generated method stub
        return null;
    }


}
