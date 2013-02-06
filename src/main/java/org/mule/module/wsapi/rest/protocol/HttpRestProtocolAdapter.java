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
import org.mule.module.wsapi.rest.action.ActionType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpRestProtocolAdapter implements RestProtocolAdapter
{
    private ActionType actionType;
    private URI resourceURI;
    private String acceptHeader;
    private String contentType;
    private Map<String, Object> queryParams;
    
    public HttpRestProtocolAdapter(MuleEvent event)
    {
        URI baseUri = event.getMessageSourceURI();
        if( event.getMessage().getInboundProperty("Host") != null )
        {
            String hostHeader = event.getMessage().getInboundProperty("Host");
            if( hostHeader.indexOf(':') != -1 )
            {
                String host = hostHeader.substring(0, hostHeader.indexOf(':'));
                int port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':')+1));
                try
                {
                    String requestPath;
                    requestPath = (String)event.getMessage().getInboundProperty("http.request.path");
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
                    requestPath = (String)event.getMessage().getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, (String)event.getMessage().getInboundProperty("Host"), 80, requestPath, null, null);
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
                this.resourceURI = new URI("http", null, baseUri.getHost(), baseUri.getPort(), (String)event.getMessage().getInboundProperty("http.request.path"), null, null);
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
        if( this.contentType != null && this.contentType.indexOf(";") != -1 )
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
    public void statusResourceNotFound(MuleEvent muleEvent)
    {
        muleEvent.getMessage().setOutboundProperty("http.status", 404);
    }

    @Override
    public void statusActionNotAllowed(MuleEvent muleEvent)
    {
        muleEvent.getMessage().setOutboundProperty("http.status", 405);
    }

}


