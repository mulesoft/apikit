/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;

public final class RestProtocolAdapterFactory
{
    private static RestProtocolAdapterFactory INSTANCE;

    static
    {
        INSTANCE = new RestProtocolAdapterFactory();
    }

    private RestProtocolAdapterFactory()
    {
    }

    public static RestProtocolAdapterFactory getInstance()
    {
        return INSTANCE;
    }

    public RestProtocolAdapter getAdapterForEvent(MuleEvent event)
    {
        String scheme = event.getMessageSourceURI().getScheme();
        if (scheme.equals("http") || scheme.equals("https") ||
            scheme.equals("niohttp") || scheme.equals("niohttps"))
        {
            return new HttpRestProtocolAdapter(event);
        }
        else
        {
            throw new IllegalArgumentException("Unable to map REST concepts for scheme " + scheme);
        }
    }
}
