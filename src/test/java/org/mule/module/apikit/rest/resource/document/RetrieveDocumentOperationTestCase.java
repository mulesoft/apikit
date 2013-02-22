
package org.mule.module.apikit.rest.resource.document;

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
public class RetrieveDocumentOperationTestCase extends AbstractMuleTestCase
{
    @Mock
    MessageProcessor handler;
    @Mock
    MuleEvent event;
    @Mock
    MuleContext muleContext;
    @Mock
    RestRequest restRequest;
    @Mock
    RestWebService service;
    RetrieveDocumentOperation operation;

    @Before
    public void setup() throws MuleException
    {
        when(restRequest.getService()).thenReturn(service);
        when(restRequest.getMuleEvent()).thenReturn(event);
        when(service.getMuleContext()).thenReturn(muleContext);
        operation = new RetrieveDocumentOperation();
        operation.setHandler(handler);
    }

    @Test
    public void handlerReturnsNormally() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage("", muleContext));
        assertEquals(event, operation.handle(restRequest));
    }

    @Test(expected = OperationHandlerException.class)
    public void handlerThrowsException() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenThrow(new RuntimeException());
        operation.handle(restRequest);
    }

    @Test
    public void handlerReturnsNull() throws MuleException, RestException
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(null);
        operation.handle(restRequest);
        assertNotNull(operation.handle(restRequest));
        assertEquals(NullPayload.getInstance(), operation.handle(restRequest).getMessage().getPayload());
    }

    @Test
    public void handlerReturnsNullPayload() throws Exception
    {
        when(handler.process(any(MuleEvent.class))).thenReturn(event);
        when(event.getMessage()).thenReturn(new DefaultMuleMessage(NullPayload.getInstance(), muleContext));
        operation.handle(restRequest);
        assertEquals(event, operation.handle(restRequest));
        assertEquals(NullPayload.getInstance(), operation.handle(restRequest).getMessage().getPayload());
    }

}
