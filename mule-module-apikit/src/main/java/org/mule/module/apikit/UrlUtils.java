/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.exception.ApikitRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils
{

    public static String getBaseSchemeHostPort(MuleEvent event)
    {
        String host;
        String chHost = System.getProperty("fullDomain");
        if (chHost != null)
        {
            host = chHost;
        }
        else
        {
            host = event.getMessage().getInboundProperty("host");
        }
        String endpoint = event.getMessage().getInboundProperty("http.context.uri");
        String scheme;
        if (endpoint.startsWith("http:"))
        {
            scheme = "http";
        }
        else if (endpoint.startsWith("https:"))
        {
            scheme = "https";
        }
        else
        {
            throw new ApikitRuntimeException("Unsupported scheme: " + endpoint);
        }
        return scheme + "://" + host;
    }

    public static String getBaseSchemeHostPort(String baseUri)
    {
        URL url;
        try
        {
            url = new URL(baseUri);
        }
        catch (MalformedURLException e)
        {
            return "http://localhost";
        }
        return url.getProtocol() + "://" + url.getAuthority();
    }
}
