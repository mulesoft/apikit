/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import static org.mule.api.config.MuleProperties.CONTENT_TYPE_PROPERTY;
import static org.mule.module.apikit.rest.representation.RepresentationMetaData.MULE_RESPONSE_MEDIATYPE_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapterFactory;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import com.google.common.net.MediaType;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class DefaultRestRequest implements RestRequest
{

    protected MuleEvent event;
    protected RestProtocolAdapter protocolAdapter;
    protected Deque<String> pathStack;
    protected RestWebService restWebService;
    protected String relativeURI;

    public DefaultRestRequest(MuleEvent event, RestWebService restWebService)
    {
        this.event = event;
        this.protocolAdapter = createProtocolAdapter(event);
        this.restWebService = restWebService;
        relativeURI = calculateRelativeURI();
        initPathStack();
    }

    protected String calculateRelativeURI()
    {
        String baseURIPath = protocolAdapter.getBaseURI().getPath();
        String requestURIPath = protocolAdapter.getURI().getPath();
        return StringUtils.difference(baseURIPath, requestURIPath);
    }

    protected RestProtocolAdapter createProtocolAdapter(MuleEvent event)
    {
        return RestProtocolAdapterFactory.getInstance().getAdapterForEvent(event);
    }

    private void initPathStack()
    {
        Deque<String> pathStack = new ArrayDeque<String>(Arrays.asList(relativeURI.split("/")));
        if (!pathStack.isEmpty() && pathStack.peekFirst().isEmpty())
        {
            pathStack.removeFirst();
        }
        this.pathStack = pathStack;
    }

    @Override
    public MuleEvent getMuleEvent()
    {
        return event;
    }

    @Override
    public String getNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            return "";
        }
        return pathStack.removeFirst();
    }
    
    @Override
    public String peekNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            return "";
        }
        return pathStack.peekFirst();
    }

    @Override
    public boolean hasMorePathElements()
    {
        return !pathStack.isEmpty() && !pathStack.getFirst().equals("");
    }

    @Override
    public RestProtocolAdapter getProtocolAdaptor()
    {
        return protocolAdapter;
    }

    public RestWebService getService()
    {
        return restWebService;
    }

    @Override
    public void setErrorPayload(String uri, String title, String detail, String statusCode)
    {
        String responseMediaType = getMuleEvent().getMessage().getOutboundProperty(MULE_RESPONSE_MEDIATYPE_PROPERTY);
        if (responseMediaType == null)
        {
            responseMediaType = "notSet";
        }
        if (responseMediaType.equals(MediaType.JSON_UTF_8.withoutParameters().toString()))
        {
            String response = "{\"describedBy\":\"" + uri + "\"," +
            "\"title\":\"" + title + "\"," +
            "\"detail\":\"" + detail + "\"," +
            "\"httpStatus\":\"" + statusCode + "\"}";
            getMuleEvent().getMessage().setPayload(response);
            getMuleEvent().getMessage().setOutboundProperty(CONTENT_TYPE_PROPERTY, responseMediaType);
        }
        else if (responseMediaType.equals(MediaType.XML_UTF_8.withoutParameters().toString()))
        {
            String response = "<problem><describedBy>" + uri + "</describedBy>" +
            "<title>" + title + "</title>" +
            "<detail>" + detail + "</detail>" +
            "<httpStatus>" + statusCode + "</httpStatus></problem>";
            getMuleEvent().getMessage().setPayload(response);
            getMuleEvent().getMessage().setOutboundProperty(CONTENT_TYPE_PROPERTY, responseMediaType);
        }
        else
        {
            //TODO plain/text or nothing at all?
            getMuleEvent().getMessage().setPayload(NullPayload.getInstance());
        }
    }

}
