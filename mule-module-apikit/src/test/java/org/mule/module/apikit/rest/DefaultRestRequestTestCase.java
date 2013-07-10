/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.rest.protocol.RestProtocolAdapter;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.tck.size.SmallTest;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class DefaultRestRequestTestCase
{

    @Mock
    MuleEvent event;
    @Mock
    MuleMessage message;
    @Mock
    RestWebService restWebService;
    @Mock
    HttpRestProtocolAdapter httpAdapter;

    RestRequest request;

    @Test
    public void singleElementBasePathRootRequest() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void singleElementBasePathRootRequest2() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api/"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void singleElementBasePathRootRequest3() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api/"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void singleElementBasePathRootRequest4() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void singleElementBasePathSingleRequest() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/leagues"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void twoElementBasePathSingleRequest() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api/v1"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/v1/leagues"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void singleElementBasePathMultipleRequest() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/leagues/1"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertEquals("1", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void twoElementBasePathMultipleRequest() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://localhost:5555/api/v1.0"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/v1.0/leagues/1"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertEquals("1", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void withNioAsProtocol() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("niohttp://localhost:5555/api/v1.0"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://localhost:5555/api/v1.0/leagues/1"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertEquals("1", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void withNonMatchingHostname() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://0.0.0.0:5555/api"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://127.0.0.1:5555/api/leagues/1"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertEquals("1", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    @Test
    public void withoutPath() throws URISyntaxException
    {
        when(httpAdapter.getBaseURI()).thenReturn(new URI("http://0.0.0.0:5555"));
        when(httpAdapter.getURI()).thenReturn(new URI("http://127.0.0.1:5555/leagues/1"));
        request = new TestRestRequest(event, restWebService);
        assertEquals("leagues", request.getNextPathElement());
        assertEquals("1", request.getNextPathElement());
        assertFalse(request.hasMorePathElements());
    }

    class TestRestRequest extends DefaultRestRequest
    {

        public TestRestRequest(MuleEvent event, RestWebService restWebService)
        {
            super(event, restWebService);
        }

        @Override
        protected RestProtocolAdapter createProtocolAdapter(MuleEvent event)
        {
            return httpAdapter;
        }
    }
}
