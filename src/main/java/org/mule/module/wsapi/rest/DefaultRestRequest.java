/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest;

import org.mule.api.MuleEvent;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapter;
import org.mule.module.wsapi.rest.protocol.RestProtocolAdapterFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class DefaultRestRequest implements RestRequest
{

    protected MuleEvent muleEvent;
    protected RestProtocolAdapter protocolAdapter;
    protected Deque<String> pathStack;

    public DefaultRestRequest(MuleEvent event)
    {
        this.muleEvent = event;
        this.protocolAdapter = RestProtocolAdapterFactory.getInstance().getAdapterForEvent(event);
        initPathStack();
    }

    private void initPathStack()
    {
        Deque<String> pathStack = new ArrayDeque<String>(Arrays.asList(protocolAdapter.getURI()
            .getPath()
            .split("/")));
        // TODO: does not work with base paths with more than one element e.g: /a/b
        String baseURIPath = protocolAdapter.getBaseURI().getPath().substring(1);

        String pathElement = pathStack.removeFirst();
        while (!pathElement.equals(baseURIPath))
        {
            pathElement = pathStack.removeFirst();
        }
        this.pathStack = pathStack;
    }

    @Override
    public MuleEvent getMuleEvent()
    {
        return muleEvent;
    }

    @Override
    public String getNextPathElement()
    {
        if (pathStack.isEmpty())
        {
            return null;
        }
        return pathStack.removeFirst();
    }

    @Override
    public boolean hasMorePathElements()
    {
        return !pathStack.isEmpty();
    }

    @Override
    public RestProtocolAdapter getProtocolAdaptor()
    {
        return protocolAdapter;
    }

}
