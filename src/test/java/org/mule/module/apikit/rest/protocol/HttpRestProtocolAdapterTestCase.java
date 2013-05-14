/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.module.apikit.rest.representation.RepresentationMetaData.MULE_RESPONSE_MEDIATYPE_PROPERTY;
import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.rest.DefaultRestRequest;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.module.apikit.rest.resource.ResourceNotFoundException;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.transport.http.HttpConnector;

import com.google.common.net.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class HttpRestProtocolAdapterTestCase extends AbstractMuleTestCase
{
    @Mock
    DefaultRestRequest request;
    @Mock
    MuleEvent event;
    @Mock
    MuleMessage message;
    HttpRestProtocolAdapter adapter;

    @Before
    public void setup() throws URISyntaxException
    {
        when(request.getMuleEvent()).thenReturn(event);
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
            "/api/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://localhost:8080/api/orders/1/address", adapter.getURI().toString());
    }

    @Test
    public void resourceUriHostHeader()
    {
        when(message.getInboundProperty("host")).thenReturn("otherhost");
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
        when(message.getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)).thenReturn(
            "/api/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://otherhost:80/api/orders/1/address", adapter.getURI().toString());
    }

    @Test
    public void resourceUriHostHeaderWithPort()
    {
        when(message.getInboundProperty("host")).thenReturn("otherhost:8080");
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("get");
        when(message.getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY)).thenReturn(
            "/api/orders/1/address");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals("http://otherhost:8080/api/orders/1/address", adapter.getURI().toString());
    }

    @Test
    public void retrieveActionType()
    {
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(RestOperationType.RETRIEVE, adapter.getOperationType());
    }

    @Test
    public void createActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("post");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(RestOperationType.CREATE, adapter.getOperationType());
    }

    @Test
    public void updateActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("put");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(RestOperationType.UPDATE, adapter.getOperationType());
    }

    @Test
    public void existsActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("head");
        adapter = new HttpRestProtocolAdapter(event);

        assertEquals(RestOperationType.EXISTS, adapter.getOperationType());
    }

    @Test
    public void deleteActionType()
    {
        when(message.getInboundProperty(HttpConnector.HTTP_METHOD_PROPERTY)).thenReturn("delete");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(RestOperationType.DELETE, adapter.getOperationType());
    }

    @Test
    public void acceptedContentTypes()
    {
        when(message.getInboundProperty("accept")).thenReturn("text/html");

        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(1, adapter.getAcceptableResponseMediaTypes().size());
        assertTrue(MediaType.HTML_UTF_8.is(adapter.getAcceptableResponseMediaTypes().get(0).withoutParameters()));
    }

    @Test
    public void requestContentType()
    {
        when(message.getInboundProperty("content-type")).thenReturn("text/html");

        adapter = new HttpRestProtocolAdapter(event);
        assertTrue(MediaType.HTML_UTF_8.is(adapter.getRequestMediaType()));
    }

    @Test
    public void queryParameters()
    {
        Map<String, Object> queryParams = new HashMap<String, Object>();
        when(message.getInboundProperty(HttpConnector.HTTP_QUERY_PARAMS)).thenReturn(queryParams);
        adapter = new HttpRestProtocolAdapter(event);
        assertEquals(queryParams, adapter.getQueryParameters());
    }

    @Test
    public void handleResourceNotFoundException()
    {
        adapter = new HttpRestProtocolAdapter(event);
        adapter.handleException(new ResourceNotFoundException("1"), request);
        verify(message).setOutboundProperty(HTTP_STATUS_PROPERTY, 404);
    }

    @Test
    public void handleServerExceptionJson()
    {
        doCallRealMethod().when(request).setErrorPayload(anyString(), anyString(), anyString(), anyString());
        when(message.getOutboundProperty(MULE_RESPONSE_MEDIATYPE_PROPERTY)).thenReturn("application/json");
        adapter = new HttpRestProtocolAdapter(event);
        adapter.handleException(new OperationHandlerException(new Exception("server error")), request);
        verify(message).setOutboundProperty(HTTP_STATUS_PROPERTY, 500);
        ArgumentCaptor<Object> arg = ArgumentCaptor.forClass(Object.class);
        verify(message).setPayload(arg.capture());
        assertTrue("unexpected payload: " + arg.getValue(), ((String) arg.getValue()).matches("\\{.*\"httpStatus\":\"500\".*}"));

    }

    @Test
    public void handleServerExceptionXml()
    {
        doCallRealMethod().when(request).setErrorPayload(anyString(), anyString(), anyString(), anyString());
        when(message.getOutboundProperty(MULE_RESPONSE_MEDIATYPE_PROPERTY)).thenReturn("text/xml");
        adapter = new HttpRestProtocolAdapter(event);
        adapter.handleException(new OperationHandlerException(new Exception("server error")), request);
        verify(message).setOutboundProperty(HTTP_STATUS_PROPERTY, 500);
        //verify(message).setOutboundProperty(CONTENT_TYPE_PROPERTY, "text/xml");
        ArgumentCaptor<Object> arg = ArgumentCaptor.forClass(Object.class);
        verify(message).setPayload(arg.capture());
        assertTrue("unexpected payload: " + arg.getValue(), ((String) arg.getValue()).matches("<problem>.*<httpStatus>500</httpStatus>.*</problem>"));
    }
}
