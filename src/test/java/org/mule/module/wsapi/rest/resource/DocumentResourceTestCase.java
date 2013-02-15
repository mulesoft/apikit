package org.mule.module.wsapi.rest.resource;

import static java.lang.Boolean.TRUE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.wsapi.rest.RestException;
import org.mule.module.wsapi.rest.RestRequest;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.action.RestRetrieveAction;
import org.mule.module.wsapi.rest.protocol.HttpRestProtocolAdapter;
import org.mule.module.wsapi.rest.resource.DocumentResource;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class DocumentResourceTestCase extends AbstractMuleTestCase
{
    @Mock MuleEvent event;
    @Mock MuleMessage message;
    @Mock RestRequest request;
    @Mock HttpRestProtocolAdapter httpAdapter;
    DocumentResource doc;

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter).handleException(any(RestException.class), any(MuleEvent.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        doc = new DocumentResource("doc");
    }

    @Test
    public void suportedActionTypes(){
        
        System.out.println(doc.getSupportedActionTypes());
        
    }

    
    @Test
    public void handleHttpResourceFound() throws RestException
    {
        RestAction action = mock(RestRetrieveAction.class);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);
        doc.setActions(Collections.singletonList(action));
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);

        doc.handle(request);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(MuleEvent.class));
    }

    @Test
    public void handleHttpResourceFoundActionNotSupported() throws RestException
    {
        doc.handle(request);
        verify(message).setOutboundProperty("http.status", 405);
    }

    @Test
    public void handleHttpResourceNotFound() throws RestException
    {
        when(request.hasMorePathElements()).thenReturn(TRUE);
        when(request.getNextPathElement()).thenReturn("path");
        doc.handle(request);
        verify(message).setOutboundProperty("http.status", 404);
    }

}