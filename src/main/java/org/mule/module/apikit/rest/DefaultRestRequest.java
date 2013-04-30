/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapterFactory;
import org.mule.util.StringUtils;

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

}
