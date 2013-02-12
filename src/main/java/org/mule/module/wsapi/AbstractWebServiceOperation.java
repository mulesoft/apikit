/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi;

import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.api.WebServiceOperation;

public class AbstractWebServiceOperation implements WebServiceOperation
{

    protected String name;
    protected MessageProcessor handler;

    public AbstractWebServiceOperation(String name, MessageProcessor handler)
    {
        this.name = name;
        this.handler = handler;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public MessageProcessor getHandler()
    {
        return handler;
    }

}
