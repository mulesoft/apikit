/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.api.WebServiceOperation;

public class AbstractWebServiceOperation implements WebServiceOperation
{

    protected MessageProcessor handler;
    protected String accessExpression;
    protected String description = "";

    @Override
    public MessageProcessor getHandler()
    {
        return handler;
    }

    public void setHandler(MessageProcessor handler)
    {
        this.handler = handler;
    }

    @Override
    public String getAccessExpression()
    {
        return accessExpression;
    }

    public void setAccessExpression(String accessExpression)
    {
        this.accessExpression = accessExpression;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

}
