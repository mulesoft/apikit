/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class HttpRestProtocolAdapterTestCase extends AbstractMuleTestCase
{
    @Mock
    MuleEvent event;
    @Mock
    MuleMessage message;;
    HttpRestProtocolAdapter adapter;

    @Before
    public void setup() throws URISyntaxException
    {
        when(event.getMessageSourceURI()).thenReturn(new URI("http://localhost:8080/api"));
        when(message.getInboundProperty("http.method")).thenReturn("get");
        when(event.getMessage()).thenReturn(message);
    }

    @Test
    public void baseUri()
    {
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://localhost:8080/api", adapter.getBaseURI().toString());
    }

    @Test
    public void resourceUri()
    {
        when(message.getInboundProperty("http.request.path")).thenReturn("/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://localhost:8080/api/orders/1/address", adapter.getURI().toString());
    }
    
    @Test
    public void resourceUriHostHeader()
    {
        when(message.getInboundProperty("http.request.path")).thenReturn("/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://otherhost:8080/api/orders/1/address", adapter.getURI().toString());
    }

    
    @Test
    public void retrieveActionType()
    {
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.RETRIEVE, adapter.getActionType());
    }

    @Test
    public void createActionType()
    {
        when(message.getInboundProperty("http.method")).thenReturn("post");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.CREATE, adapter.getActionType());
    }

    @Test
    public void updateActionType()
    {
        when(message.getInboundProperty("http.method")).thenReturn("put");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.UPDATE, adapter.getActionType());
    }

    @Test
    public void existsActionType()
    {
        when(message.getInboundProperty("http.method")).thenReturn("head");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.EXISTS, adapter.getActionType());
    }

    @Test
    public void deleteActionType()
    {
        when(message.getInboundProperty("http.method")).thenReturn("delete");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(ActionType.DELETE, adapter.getActionType());
    }

}
