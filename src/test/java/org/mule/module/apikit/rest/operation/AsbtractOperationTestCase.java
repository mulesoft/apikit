
package org.mule.module.apikit.rest.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.transport.NullPayload;

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

    @Before
    public void setup() throws MuleException
    {
        when(restRequest.getService()).thenReturn(service);
        when(restRequest.getMuleEvent()).thenReturn(event);
        when(service.getMuleContext()).thenReturn(muleContext);
    }

    public abstract RestOperation getOperation();

    @Test
    public void handlerReturnsNormally() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage("", muleContext));
        assertEquals(event, getOperation().handle(restRequest));
    }

    @Test(expected = OperationHandlerException.class)
    public void handlerThrowsException() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenThrow(new RuntimeException());
        getOperation().handle(restRequest);
    }

    @Test
    public void handlerReturnsNull() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(null);
        getOperation().handle(restRequest);
        assertNotNull(getOperation().handle(restRequest));
        assertEquals(NullPayload.getInstance(), getOperation().handle(restRequest).getMessage().getPayload());
    }

    @Test
    public void handlerReturnsNullPayload() throws Exception
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage(NullPayload.getInstance(), muleContext));
        getOperation().handle(restRequest);
        assertEquals(event, getOperation().handle(restRequest));
        assertEquals(NullPayload.getInstance(), getOperation().handle(restRequest).getMessage().getPayload());
    }

}
