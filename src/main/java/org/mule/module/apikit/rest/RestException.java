/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.protocol.http.HttpStatusCode;
import org.mule.transport.NullPayload;

public class RestException extends Exception
{

    private static final long serialVersionUID = -8161118644314571187L;

    public RestException()
    {
        super();
    }

    public RestException(String message)
    {
        super(message);
    }

    public RestException(Throwable cause)
    {
        super(cause);
    }

    public MuleEvent updateMuleEvent(MuleEvent event)
    {
        String responseType = "application/json";
        String accept = event.getMessage().getInboundProperty("accept");
        if (accept != null)
        {
            responseType = accept.split(",")[0];
        }
        event.getMessage().setOutboundProperty("Content-Type", responseType);
        event.getMessage().setOutboundProperty(HTTP_STATUS_PROPERTY, getStatus().getCode());
        //TODO set error description on payload
        event.getMessage().setPayload(NullPayload.getInstance());
        return event;
    }

    public HttpStatusCode getStatus()
    {
        return HttpStatusCode.SERVER_ERROR_INTERNAL;
    }
}
