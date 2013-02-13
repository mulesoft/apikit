/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest.protocol;

import org.mule.api.MuleEvent;
import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.action.ActionNotSupportedException;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.resource.ResourceNotFoundException;
import org.mule.transport.NullPayload;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpRestProtocolAdapter implements RestProtocolAdapter
{
    private URI baseURI;
    private ActionType actionType;
    private URI resourceURI;
    private String acceptHeader;
    private String contentType;
    private Map<String, Object> queryParams;
    protected MuleEvent event;

    public HttpRestProtocolAdapter(MuleEvent event)
    {
        this.event = event;
        this.baseURI = event.getMessageSourceURI();
        if (event.getMessage().getInboundProperty("Host") != null)
        {
            String hostHeader = event.getMessage().getInboundProperty("Host");
            if (hostHeader.indexOf(':') != -1)
            {
                String host = hostHeader.substring(0, hostHeader.indexOf(':'));
                int port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
                try
                {
                    String requestPath;
                    requestPath = (String) event.getMessage().getInboundProperty("http.request.path");
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
                    requestPath = (String) event.getMessage().getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, (String) event.getMessage().getInboundProperty(
                        "Host"), 80, requestPath, null, null);
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
                    (String) event.getMessage().getInboundProperty("http.request.path"), null, null);
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException("Cannot parse URI", e);
            }
        }
        String method = event.getMessage().getInboundProperty("http.method");
        actionType = ActionType.fromHttpMethod(method);
        this.acceptHeader = event.getMessage().getInboundProperty("Accept");

        this.contentType = event.getMessage().getInboundProperty("Content-Type");
        if (this.contentType != null && this.contentType.indexOf(";") != -1)
        {
            this.contentType = this.contentType.substring(0, this.contentType.indexOf(";"));
        }

        this.queryParams = event.getMessage().getInboundProperty("http.query.params");
    }

    @Override
    public ActionType getActionType()
    {
        return actionType;
    }

    @Override
    public URI getURI()
    {
        return resourceURI;
    }

    @Override
    public URI getBaseURI()
    {
        return baseURI;
    }

    @Override
    public String getAcceptedContentTypes()
    {
        return acceptHeader;
    }

    @Override
    public String getRequestContentType()
    {
        return contentType;
    }

    @Override
    public Map<String, Object> getQueryParameters()
    {
        return queryParams;
    }

    @Override
    public void handleException(RestException re)
    {
        if (re instanceof ActionNotSupportedException)
        {
            event.getMessage().setOutboundProperty("http.status", 405);
            event.getMessage().setPayload(NullPayload.getInstance());

        }
        else if (re instanceof ResourceNotFoundException)
        {
            event.getMessage().setOutboundProperty("http.status", 404);
            event.getMessage().setPayload(NullPayload.getInstance());
        }
        else if (re instanceof MediaTypeNotAcceptable)
        {
            event.getMessage().setOutboundProperty("http.status", 406);

        }
        else
        {

        }

    }

}
