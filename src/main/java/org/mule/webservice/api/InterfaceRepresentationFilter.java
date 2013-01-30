/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.api;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.processor.AbstractInterceptingMessageProcessor;

public class InterfaceRepresentationFilter extends AbstractInterceptingMessageProcessor
{

    private String representionPattern;

    public InterfaceRepresentationFilter(String representionPattern, WebServiceInterface webServiceInterface)
    {
        this.representionPattern = representionPattern;
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        String requestUri = event.getMessage().getInboundProperty("http.request");
        if (requestUri.endsWith(representionPattern))
        {
            return new DefaultMuleEvent(new DefaultMuleMessage("<wsdl>", muleContext), event);
        }
        else
        {
            return processNext(event);
        }
    }

}
