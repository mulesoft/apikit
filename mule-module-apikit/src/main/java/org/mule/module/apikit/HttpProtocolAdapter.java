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

    private String basePath;
    private URI resourceURI;
    private String method;
    private String acceptableResponseMediaTypes;
    private String requestMediaType;
    private Map<String, Object> queryParams;

    public HttpProtocolAdapter(MuleEvent event)
    {
        MuleMessage message = event.getMessage();
        this.basePath = UrlUtils.getBasePath(message);
        String hostHeader = message.getInboundProperty("host");
        if (hostHeader == null)
        {
            throw new IllegalArgumentException("host header cannot be null");
        }
        String host = hostHeader;
        int port = 80;
        String requestPath = message.getInboundProperty(HTTP_REQUEST_PATH_PROPERTY);
        if (hostHeader.contains(":"))
        {
            host = hostHeader.substring(0, hostHeader.indexOf(':'));
            port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
        }
        try
        {
            this.resourceURI = new URI("http", null, host, port, requestPath, null, null);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Cannot parse URI", e);
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

    public String getBasePath()
    {
        return basePath;
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
        if (acceptableResponseMediaTypes == null)
        {
            return "*/*";
        }
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
