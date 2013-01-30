/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.ws;

import org.mule.api.processor.MessageProcessor;
import org.mule.webservice.AbstractWebServiceOperation;

public class WSDLOperation extends AbstractWebServiceOperation
{

    public WSDLOperation(String name, MessageProcessor handler)
    {
        super(name, handler);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public MessageProcessor getHandler()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
