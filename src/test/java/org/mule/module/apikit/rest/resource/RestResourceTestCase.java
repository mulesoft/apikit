/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionManager;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.OperationNotAllowedException;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class RestResourceTestCase extends AbstractMuleTestCase
{

    @Mock
    protected MuleEvent event;
    @Mock
    protected MuleMessage message;
    @Mock
    protected RestRequest request;
    @Mock
    protected HttpRestProtocolAdapter httpAdapter;
    @Mock
    protected RestOperation action;
    @Mock
    protected MuleContext muleContext;
    @Mock
    protected ExpressionManager expressionManager;
    protected AbstractRestResource resource;

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter)
            .handleException(any(RestException.class), any(RestRequest.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        when(event.getMuleContext()).thenReturn(muleContext);
        when(muleContext.getExpressionManager()).thenReturn(expressionManager);
        resource = new DummyRestResource("doc");
        resource.setActions(Collections.singletonList(action));
        when(expressionManager.evaluateBoolean(Mockito.eq("#[true]"), any(MuleEvent.class))).thenReturn(
            Boolean.TRUE);
        when(expressionManager.evaluateBoolean(Mockito.eq("#[false]"), any(MuleEvent.class))).thenReturn(
            Boolean.FALSE);
    }

    @Test
    public void actionTypeAllowed() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);
        when(action.getType()).thenReturn(RestOperationType.RETRIEVE);

        resource.handle(request);

        verify(action).handle(request);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(RestRequest.class));
        verify(message, never());
    }

    @Test
    public void actionTypeNotAllowed() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);
        when(action.getType()).thenReturn(RestOperationType.UPDATE);

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(OperationNotAllowedException.class),
            any(RestRequest.class));
        verify(message).setOutboundProperty("http.status", 405);
    }

    @Test
    public void resourceAuthorized() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);
        when(action.getType()).thenReturn(RestOperationType.RETRIEVE);

        resource.setAccessExpression("#[true]");

        resource.handle(request);

        verify(action).handle(request);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(RestRequest.class));
        verify(message, never());
    }

    @Test
    public void resourceNotAuthorized() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);
        when(action.getType()).thenReturn(RestOperationType.RETRIEVE);

        resource.setAccessExpression("#[false]");

        resource.handle(request);

        verify(action, never()).handle(request);
        verify(httpAdapter, times(1)).handleException(any(RestException.class), any(RestRequest.class));
        verify(message).setOutboundProperty("http.status", 401);
    }

    @Test
    public void getAuthorizedActions() throws RestException, MuleException
    {
        RestOperation action1 = mock(RestOperation.class);
        when(action1.getAccessExpression()).thenReturn("#[true]");
        RestOperation action2 = mock(RestOperation.class);
        when(action2.getAccessExpression()).thenReturn("#[false]");

        List<RestOperation> actions = new ArrayList<RestOperation>();
        actions.add(action1);
        actions.add(action2);
        resource.setActions(actions);

        List<RestOperation> authorizedActions = resource.getAuthorizedActions(request);
        assertEquals(1, authorizedActions.size());
        assertEquals(action1, authorizedActions.get(0));
    }

    static class DummyRestResource extends AbstractRestResource
    {
        public DummyRestResource(String name)
        {
            super(name);
        }

        @Override
        protected Set<RestOperationType> getSupportedActionTypes()
        {
            return EnumSet.of(RestOperationType.RETRIEVE);
        }
    }

}
