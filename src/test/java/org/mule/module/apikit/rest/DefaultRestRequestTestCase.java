
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
