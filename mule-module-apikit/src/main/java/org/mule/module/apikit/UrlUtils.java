/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.transport.http.HttpConnector.HTTP_CONTEXT_PATH_PROPERTY;
import static org.mule.transport.http.HttpConnector.HTTP_REQUEST_PATH_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.http.internal.ParameterMap;
import org.mule.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;

public class UrlUtils
{

    public static String getBaseSchemeHostPort(MuleEvent event)
    {
        String host = event.getMessage().getInboundProperty("host");
        String chHost = System.getProperty("fullDomain");
        if (chHost != null)
        {
            host = chHost;
        }
        return getScheme(event.getMessage()) + "://" + host;
    }

    public static String getScheme(MuleMessage message)
    {
        String scheme = message.getInboundProperty("http.scheme");
        if (scheme == null)
        {
            String endpoint = message.getInboundProperty("http.context.uri");
            if (endpoint == null)
            {
                throw new ApikitRuntimeException("Cannot figure out the request scheme");
            }
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
        }
        return scheme;
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

    public static String getResourceRelativePath(MuleMessage message)
    {
        String path = message.getInboundProperty(HTTP_REQUEST_PATH_PROPERTY);
        String basePath = getBasePath(message);
        path = path.substring(basePath.length());
        if (!path.startsWith("/") && !path.isEmpty())
        {
            path = "/" + path;
        }
        return path;
    }

    public static String getBasePath(MuleMessage message)
    {
        String path = message.getInboundProperty(HTTP_CONTEXT_PATH_PROPERTY);
        if (path == null)
        {
            path = message.getInboundProperty("http.listener.path");

            if (path != null) {
                if (path.endsWith("/*"))
                {
                    path = path.substring(0, path.length() - 2);
                }

                ParameterMap uriParams = message.getInboundProperty("http.uri.params");
                if (!uriParams.isEmpty()) {
                    for (Entry<String, String> entry : uriParams.entrySet()) {
                        String uriParameter = "{" + entry.getKey() + "}";
                        if (path.contains(uriParameter)) path = path.replace(uriParameter, entry.getValue());
                    }
                }
            }
            if (path == null)
            {
                throw new IllegalArgumentException("Cannot resolve request base path");
            }
        }
        return path;
    }

    public static String getQueryString(MuleMessage message)
    {
        String queryString = message.getInboundProperty("http.query.string");
        return queryString == null ? "" : queryString;
    }

    public static String rewriteBaseUri(String raml, String baseSchemeHostPort)
    {
        return replaceBaseUri(raml, "https?://[^/]*", baseSchemeHostPort);
    }

    public static String replaceBaseUri(String raml, String newBaseUri)
    {
        return replaceBaseUri(raml, ".*$", newBaseUri);
    }

    private static String replaceBaseUri(String raml, String regex, String replacement)
    {
        String[] split = raml.split("\n");
        for (int i=0; i<split.length; i++)
        {
            if (split[i].startsWith("baseUri: "))
            {
                split[i] = split[i].replaceFirst(regex, replacement);
                if (!split[i].contains("baseUri: "))
                {
                    split[i] = "baseUri: " + split[i];
                }
            }
        }
        return StringUtils.join(split, "\n");
    }
}
