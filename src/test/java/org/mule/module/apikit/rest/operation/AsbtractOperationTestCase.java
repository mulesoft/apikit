/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.mule.message.DefaultExceptionPayload;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.transport.NullPayload;
import org.mule.transport.http.HttpConnector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public abstract class AsbtractOperationTestCase extends AbstractMuleTestCase
{
    @Mock
    protected MessageProcessor handler;
    @Mock
    protected MuleEvent event;
    @Mock
    protected MuleContext muleContext;
    @Mock
    protected RestRequest restRequest;
    @Mock
    protected RestWebService service;
    @Mock
    protected MuleMessage message;
    @Mock
    protected RestProtocolAdapter protocolAdapter;
    @Mock
    protected RestResource resource;
    protected AbstractRestOperation operation;

    @Before
    public void setup() throws MuleException, URISyntaxException
    {
        when(restRequest.getService()).thenReturn(service);
        when(restRequest.getMuleEvent()).thenReturn(event);
        when(restRequest.getProtocolAdaptor()).thenReturn(protocolAdapter);
        when(protocolAdapter.getURI()).thenReturn(new URI("http://localhost:8080/api/teams"));
        when(service.getMuleContext()).thenReturn(muleContext);
        when(event.getMessage()).thenReturn(message);
        when(resource.getName()).thenReturn("resourceName");
        operation = createOperation();
        operation.setResource(resource);
        operation.setHandler(handler);
    }

    @Test
    public void handlerReturnsNormally() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage("", muleContext));
        operation.handle(restRequest);
        assertEquals(event, restRequest.getMuleEvent());
    }

    @Test(expected = OperationHandlerException.class)
    public void handlerThrowsException() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenThrow(new RuntimeException());
        operation.handle(restRequest);
    }

    @Test(expected = OperationHandlerException.class)
    public void handlerReturnsWithExceptionPayload() throws Exception
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(message.getExceptionPayload()).thenReturn(
            new DefaultExceptionPayload(new RuntimeException("ERROR!!")));
        operation.handle(restRequest);
    }

    //
    // @Test
    // public void handlerReturnsNull() throws MuleException, RestException
    // {
    // when(handler.process(any(MuleEvent.class))).thenReturn(null);
    // operation.handle(restRequest);
    // assertNotNull(restRequest.getMuleEvent());
    // verify(event).setMessage(Matchers.hasProperty("payload", Matchers.equalTo(NullPayload.getInstance())));
    // }

    @Test
    public void handlerReturnsNullPayload() throws Exception
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage(NullPayload.getInstance(), muleContext));
        operation.handle(restRequest);
        assertNotNull(restRequest.getMuleEvent());
        assertEquals(NullPayload.getInstance(), restRequest.getMuleEvent().getMessage().getPayload());
    }

    @Test
    public void handlerReturnsCustomStatusCode() throws Exception
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        Map<String, Object> outboundProperties = Collections.<String, Object>singletonMap(HttpConnector.HTTP_STATUS_PROPERTY, "424");
        when(event.getMessage()).thenReturn(new DefaultMuleMessage("", outboundProperties, muleContext));
        operation.handle(restRequest);
        assertEquals(event, restRequest.getMuleEvent());
        assertEquals("424", restRequest.getMuleEvent().getMessage().getOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY));
    }

    public abstract AbstractRestOperation createOperation();

}
