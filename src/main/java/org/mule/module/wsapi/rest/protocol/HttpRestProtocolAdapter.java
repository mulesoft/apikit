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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

public class HttpRestProtocolAdapter implements RestProtocolAdapter
{
    private URI baseURI;
    private ActionType actionType;
    private URI resourceURI;
    private String acceptHeader;
    private String contentType;
    private Map<String, Object> queryParams;
    private Deque<String> pathStack;
    
    public HttpRestProtocolAdapter(MuleEvent event)
    {
        this.baseURI = event.getMessageSourceURI();
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
                this.resourceURI = new URI("http", null, baseURI.getHost(), baseURI.getPort(), (String)event.getMessage().getInboundProperty("http.request.path"), null, null);
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
        initPathStack();
    }

    private void initPathStack()
    {
        Deque<String> pathStack = new ArrayDeque<String>(Arrays.asList(resourceURI.getPath().split("/")));
        //TODO: does not work with base paths with more than one element e.g: /a/b
        String baseURIPath = baseURI.getPath().substring(1);

        String pathElement = pathStack.removeFirst();
        while (!pathElement.equals(baseURIPath))
        {
            pathElement = pathStack.removeFirst();
        }
        this.pathStack = pathStack;
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

    @Override
    public String getNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            throw new IllegalStateException("No more path elements!");
        }
        return pathStack.removeFirst();
    }

    @Override
    public boolean hasMorePathElements()
    {
        return !pathStack.isEmpty();
    }

}


