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

import java.net.URISyntaxException;
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
public class HierarchicalRestResourceTestCase extends AbstractMuleTestCase
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
    protected AbstractHierarchicalRestResource resource;

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

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
        resource = new DummyHierarchicalRestResource("doc");
        resource.setActions(Collections.singletonList(action));
        when(expressionManager.evaluateBoolean(Mockito.eq("#[true]"), any(MuleEvent.class))).thenReturn(
            Boolean.TRUE);
        when(expressionManager.evaluateBoolean(Mockito.eq("#[false]"), any(MuleEvent.class))).thenReturn(
            Boolean.FALSE);
    }

    @Test
    public void getAuthorizedNestedResources() throws RestException, MuleException
    {
        RestResource nestedResource1 = Mockito.mock(RestResource.class);
        when(nestedResource1.getName()).thenReturn("1");
        when(nestedResource1.getAccessExpression()).thenReturn("#[true]");
        RestResource nestedResource2 = Mockito.mock(RestResource.class);
        when(nestedResource2.getName()).thenReturn("2");
        when(nestedResource2.getAccessExpression()).thenReturn("#[false]");

        List<RestResource> nestedResources = new ArrayList<RestResource>();
        nestedResources.add(nestedResource1);
        nestedResources.add(nestedResource2);
        resource.setResources(nestedResources);
        resource.initialise();

        List<RestResource> authorizedResources = resource.getAuthorizedResources(request);

        assertEquals(1, authorizedResources.size());
        assertEquals(nestedResource1, authorizedResources.get(0));
    }

    @Test
    public void nestedResourceRouting() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("1");

        RestResource nestedResource1 = Mockito.mock(RestResource.class);
        when(nestedResource1.getName()).thenReturn("1");
        RestResource nestedResource2 = Mockito.mock(RestResource.class);
        when(nestedResource2.getName()).thenReturn("2");
        List<RestResource> nestedResources = new ArrayList<RestResource>();
        nestedResources.add(nestedResource1);
        nestedResources.add(nestedResource2);
        resource.setResources(nestedResources);
        resource.initialise();

        resource.handle(request);

        verify(nestedResource1).handle(request);
        verify(nestedResource2, never()).handle(any(RestRequest.class));
        verify(httpAdapter, never()).handleException(any(RestException.class), any(RestRequest.class));
        verify(message, never());
    }

    @Test
    public void nestedResourceNotFound() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("3");

        RestResource nestedResource1 = new DummyHierarchicalRestResource("1");
        RestResource nestedResource2 = new DummyHierarchicalRestResource("2");
        List<RestResource> nestedResources = new ArrayList<RestResource>();
        nestedResources.add(nestedResource1);
        nestedResources.add(nestedResource2);
        resource.setResources(nestedResources);
        resource.initialise();

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(ResourceNotFoundException.class),
            any(RestRequest.class));
        verify(message).setOutboundProperty("http.status", 404);
    }

    @Test
    public void nestedResourceTwoLevels() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE)
            .thenReturn(Boolean.TRUE)
            .thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("1").thenReturn("3");

        DummyHierarchicalRestResource nestedResource1 = new DummyHierarchicalRestResource("1");
        DummyHierarchicalRestResource nestedResource2 = new DummyHierarchicalRestResource("2");
        List<RestResource> nestedResources = new ArrayList<RestResource>();
        nestedResources.add(nestedResource1);
        nestedResources.add(nestedResource2);
        resource.setResources(nestedResources);
        RestResource nestedResource3 = Mockito.mock(RestResource.class);
        when(nestedResource3.getName()).thenReturn("3");
        RestResource nestedResource4 = Mockito.mock(RestResource.class);
        when(nestedResource4.getName()).thenReturn("4");
        List<RestResource> nestedResources2 = new ArrayList<RestResource>();
        nestedResources2.add(nestedResource3);
        nestedResources2.add(nestedResource4);
        nestedResource1.setResources(nestedResources2);
        resource.initialise();
        nestedResource1.initialise();
        nestedResource2.initialise();

        resource.handle(request);

        verify(nestedResource3).handle(request);
        verify(nestedResource4, never()).handle(any(RestRequest.class));

        verify(httpAdapter, never()).handleException(any(RestException.class), any(RestRequest.class));
        verify(message, never());
    }

    @Test
    public void nestedResourceActionTypeNotAllowed() throws RestException, MuleException, URISyntaxException
    {
        when(request.hasMorePathElements()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(request.getNextPathElement()).thenReturn("1");
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);
        when(action.getType()).thenReturn(RestOperationType.RETRIEVE);
        RestResource nestedResource = new DummyHierarchicalRestResource("1");
        RestOperation nestedResourceAction = Mockito.mock(RestOperation.class);
        when(nestedResourceAction.getType()).thenReturn(RestOperationType.UPDATE);
        nestedResource.setActions(Collections.singletonList(nestedResourceAction));
        resource.setResources(Collections.singletonList(nestedResource));
        resource.initialise();

        resource.handle(request);

        verify(httpAdapter, times(1)).handleException(any(OperationNotAllowedException.class),
            any(RestRequest.class));
        verify(message).setOutboundProperty("http.status", 405);
    }

    static class DummyHierarchicalRestResource extends AbstractHierarchicalRestResource
    {
        public DummyHierarchicalRestResource(String name)
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
