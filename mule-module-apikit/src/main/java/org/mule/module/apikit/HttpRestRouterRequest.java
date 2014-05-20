/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.VoidMuleEvent;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.FilterUnacceptedException;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.module.apikit.exception.ApikitRuntimeException;

import java.util.List;

import org.raml.model.MimeType;

public class HttpRestRouterRequest extends HttpRestRequest
{

    public HttpRestRouterRequest(MuleEvent event, AbstractConfiguration config)
    {
        super(event, config);
    }

    @Override
    protected MuleEvent processResponse(MuleEvent responseEvent, List<MimeType> responseMimeTypes, String responseRepresentation) throws TransformerException, FilterUnacceptedException
    {
        if (responseEvent == null || VoidMuleEvent.getInstance().equals(responseEvent))
        {
            throw new FilterUnacceptedException(CoreMessages.messageRejectedByFilter(), requestEvent);
        }
        MuleMessage message = responseEvent.getMessage();

        //set success status
        if (message.getOutboundProperty("http.status") == null)
        {
            int status = getSuccessStatus();
            if (status == -1)
            {
                throw new ApikitRuntimeException("No success status defined for action: " + action);
            }
            message.setOutboundProperty("http.status", getSuccessStatus());
        }

        return responseEvent;
    }
}
