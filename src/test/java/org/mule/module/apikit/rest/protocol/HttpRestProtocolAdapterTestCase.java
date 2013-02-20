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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.transport.http.HttpConnector;

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
        when(event.getMessage()).thenReturn(message);
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
    }

    @Test
    public void baseUri()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://localhost:8080/api", adapter.getBaseURI().toString());
    }

    @Test
    public void resourceUri()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
        when(message.getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)).thenReturn(
            "/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://localhost:8080/api/orders/1/address", adapter.getURI().toString());
    }

    @Test
    public void resourceUriHostHeader()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
        when(message.getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)).thenReturn(
            "/orders/1/address");
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
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("post");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.CREATE, adapter.getActionType());
    }

    @Test
    public void updateActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("put");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.UPDATE, adapter.getActionType());
    }

    @Test
    public void existsActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("head");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(ActionType.EXISTS, adapter.getActionType());
    }

    @Test
    public void deleteActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("delete");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(ActionType.DELETE, adapter.getActionType());
    }

    @Test
    public void acceptedContentTypes()
    {
        when(message.getInboundProperty("accept")).thenReturn("text/html");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals("text/html", adapter.getAcceptedContentTypes());
    }

    @Test
    public void requestContentType()
    {
        when(message.getInboundProperty("content-type")).thenReturn("text/html");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals("text/html", adapter.getRequestContentType());
    }

    @Test
    public void queryParameters()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)).thenReturn(
            "/orders/1/address?key=value&key2=value2&");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(2, adapter.getQueryParameters().size());
        assertArrayEquals(new String[]{"key", "key2"}, adapter.getQueryParameters().keySet().toArray());
        assertEquals("value", adapter.getQueryParameters().get("key"));
        assertEquals("value2", adapter.getQueryParameters().get("key2"));
    }

    @Test
    public void handleResourceNotFoundException()
    {
        adapter = new HttpRestProtocolAdapter(event);
        adapter.handleException(new ResourceNotFoundException("1"), event);
        verify(message).setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY, 404);
    }
}
