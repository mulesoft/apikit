/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol.http;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.ActionTypeNotAllowedException;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpRestProtocolAdapter implements RestProtocolAdapter
{
    private URI baseURI;
    private ActionType actionType;
    private URI resourceURI;
    private String acceptHeader;
    private String contentType;
    private Map<String, Object> queryParams;

    public HttpRestProtocolAdapter(MuleEvent event)
    {
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
        if (this.contentType == null)
        {
            this.contentType = event.getMessage().getOutboundProperty("Content-Type");
        }
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

    private Set<String> actionTypesToHttpMethods(Set<ActionType> actionTypes)
    {
        Set<String> set = new HashSet<String>();
        for (ActionType type : actionTypes)
        {
            set.add(type.toHttpMethod());
        }
        return set;
    }

    @Override
    public void handleException(RestException re, MuleEvent event)
    {
        if (re instanceof ActionTypeNotAllowedException)
        {
            ActionTypeNotAllowedException anse = (ActionTypeNotAllowedException) re;
            event.getMessage().setOutboundProperty("http.status",
                HttpStatusCode.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            event.getMessage().setOutboundProperty("Allow",
                StringUtils.join(actionTypesToHttpMethods(anse.getResource().getAllowedActionTypes()), " ,"));
            event.getMessage().setPayload(NullPayload.getInstance());

        }
        else if (re instanceof ResourceNotFoundException)
        {
            event.getMessage().setOutboundProperty("http.status", HttpStatusCode.CLIENT_ERROR_NOT_FOUND);
            event.getMessage().setPayload(NullPayload.getInstance());
        }
        else if (re instanceof MediaTypeNotAcceptableException)
        {
            event.getMessage().setOutboundProperty("http.status", HttpStatusCode.CLIENT_ERROR_NOT_ACCEPTABLE);
            event.getMessage().setPayload(NullPayload.getInstance());
        }
        else if (re instanceof UnsupportedMediaTypeException)
        {
            event.getMessage().setOutboundProperty("http.status",
                HttpStatusCode.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            event.getMessage().setPayload(NullPayload.getInstance());
        }
    }

}
