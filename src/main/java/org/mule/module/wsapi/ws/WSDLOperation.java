/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.ws;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.AbstractWebServiceOperation;

public class WSDLOperation extends AbstractWebServiceOperation implements MessageProcessor
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
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        return getHandler().process(event);
    }

}
