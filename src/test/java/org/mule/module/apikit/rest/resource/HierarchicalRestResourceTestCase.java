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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.ActionTypeNotAllowedException;
import org.mule.module.apikit.rest.action.RestAction;
import org.mule.module.apikit.rest.protocol.HttpRestProtocolAdapter;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class HierarchicalRestResourceTestCase extends AbstractMuleTestCase
{

    @Mock MuleEvent event;
    @Mock MuleMessage message;
    @Mock RestRequest request;
    @Mock HttpRestProtocolAdapter httpAdapter;
    @Mock RestAction action;
    DummyHierarchicalRestResource resource;

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter).handleException(any(RestException.class), any(MuleEvent.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        resource = new DummyHierarchicalRestResource("doc");
        resource.setActions(Collections.singletonList(action));
    }

    @Test
    public void actionTypeAllowed() throws RestException, MuleException
    {
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);

        resource.handle(request);

        verify(action).handle(request);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(MuleEvent.class));
    }

    @Test
    public void actionTypeNotAllowed() throws RestException, MuleException
    {
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.UPDATE);

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(ActionTypeNotAllowedException.class),
            any(MuleEvent.class));
    }

    @Test
    public void resourceNotFound() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE);
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(ResourceNotFoundException.class),
            any(MuleEvent.class));
    }

    @Test
    public void nestedResource() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("1");
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);

        RestResource nestedResource = new DummyHierarchicalRestResource("1");

        RestAction nestedResourceAction = Mockito.mock(RestAction.class);
        when(nestedResourceAction.getType()).thenReturn(ActionType.RETRIEVE);
        nestedResource.setActions(Collections.singletonList(nestedResourceAction));
        resource.setResources(Collections.singletonList(nestedResource));

        resource.handle(request);

        verify(nestedResourceAction).handle(request);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(MuleEvent.class));
    }

    @Test
    public void nestedResourceNotFound() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("2");
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);
        RestResource nestedResource = Mockito.mock(RestResource.class);
        when(nestedResource.getName()).thenReturn("1");
        RestAction nestedResourceAction = Mockito.mock(RestAction.class);
        when(nestedResourceAction.getType()).thenReturn(ActionType.RETRIEVE);
        when(nestedResource.getActions()).thenReturn(Collections.singletonList(nestedResourceAction));
        resource.setResources(Collections.singletonList(nestedResource));

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(ResourceNotFoundException.class),
            any(MuleEvent.class));
    }

    @Test
    public void nestedResourceActionTypeNotAllowed() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("2");
        when(httpAdapter.getActionType()).thenReturn(ActionType.RETRIEVE);
        when(action.getType()).thenReturn(ActionType.RETRIEVE);
        RestResource nestedResource = Mockito.mock(RestResource.class);
        when(nestedResource.getName()).thenReturn("1");
        RestAction nestedResourceAction = Mockito.mock(RestAction.class);
        when(nestedResourceAction.getType()).thenReturn(ActionType.UPDATE);
        when(nestedResource.getActions()).thenReturn(Collections.singletonList(nestedResourceAction));
        resource.setResources(Collections.singletonList(nestedResource));

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(ActionTypeNotAllowedException.class),
            any(MuleEvent.class));
    }

    static class DummyHierarchicalRestResource extends AbstractHierarchicalRestResource
    {
        public DummyHierarchicalRestResource(String name)
        {
            super(name);
        }

        @Override
        protected Set<ActionType> getSupportedActionTypes()
        {
            return EnumSet.of(ActionType.RETRIEVE);
        }
    }

}
