/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.transport.http.HttpConnector.HTTP_METHOD_PROPERTY;
import static org.mule.transport.http.HttpConnector.HTTP_QUERY_PARAMS;
import static org.mule.transport.http.HttpConnector.HTTP_REQUEST_PATH_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpProtocolAdapter
{

    private URI baseURI;
    private URI resourceURI;
    private String method;
    private String acceptableResponseMediaTypes;
    private String requestMediaType;
    private Map<String, Object> queryParams;

    public HttpProtocolAdapter(MuleEvent event)
    {
        MuleMessage message = event.getMessage();
        this.baseURI = event.getMessageSourceURI();
        if (message.getInboundProperty("host") != null)
        {
            String hostHeader = message.getInboundProperty("host");
            if (hostHeader.indexOf(':') != -1)
            {
                String host = hostHeader.substring(0, hostHeader.indexOf(':'));
                int port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty(HTTP_REQUEST_PATH_PROPERTY);
                    this.resourceURI = new URI("http", null, host, port, requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
            else
            {
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty(HTTP_REQUEST_PATH_PROPERTY);
                    this.resourceURI = new URI("http", null, (String) message.getInboundProperty("host"), 80,
                                               requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
        }
        else
        {
            try
            {
                this.resourceURI = new URI("http", null, baseURI.getHost(), baseURI.getPort(),
                                           (String) message.getInboundProperty(HTTP_REQUEST_PATH_PROPERTY), null, null);
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException("Cannot parse URI", e);
            }
        }
        method = message.getInboundProperty(HTTP_METHOD_PROPERTY);

        if (!StringUtils.isBlank((String) message.getInboundProperty("accept")))
        {
            this.acceptableResponseMediaTypes = message.getInboundProperty("accept");
        }

        if (!StringUtils.isBlank((String) message.getInboundProperty("content-type")))
        {
            this.requestMediaType = message.getInboundProperty("content-type");
        }
        if (this.requestMediaType == null
            && !StringUtils.isBlank((String) message.getOutboundProperty("content-type")))
        {
            this.requestMediaType = message.getOutboundProperty("content-type");
        }

        this.queryParams = message.getInboundProperty(HTTP_QUERY_PARAMS);
    }

    public URI getBaseURI()
    {
        return baseURI;
    }

    public URI getResourceURI()
    {
        return resourceURI;
    }

    public String getMethod()
    {
        return method;
    }

    public String getAcceptableResponseMediaTypes()
    {
        return acceptableResponseMediaTypes;
    }

    public String getRequestMediaType()
    {
        return requestMediaType != null ? requestMediaType.split(";")[0] : null;
    }

    public Map<String, Object> getQueryParams()
    {
        return queryParams;
    }
}
