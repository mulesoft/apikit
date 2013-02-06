/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.wsapi.rest.protocol;

import org.mule.api.MuleEvent;

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
