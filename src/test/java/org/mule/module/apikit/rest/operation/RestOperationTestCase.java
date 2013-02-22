/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionManager;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.module.apikit.rest.representation.DefaultRepresentationMetaData;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import com.google.common.net.MediaType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class RestOperationTestCase extends AbstractMuleTestCase
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
    protected MuleContext muleContext;
    @Mock
    protected ExpressionManager expressionManager;
    @Mock
    protected MessageProcessor handler;
    @Mock
    RestWebService service;

    AbstractRestOperation action = new DummyRestAction();

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter)
            .handleException(any(RestException.class), any(RestRequest.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        when(request.getService()).thenReturn(service);
        when(service.getMuleContext()).thenReturn(muleContext);
        when(muleContext.getExpressionManager()).thenReturn(expressionManager);
        action.setHandler(handler);

    }

    @Test
    public void actionAuthorized() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);

        action.setAccessExpression("#[true]");
        when(expressionManager.evaluateBoolean("#[true]", event)).thenReturn(Boolean.TRUE);

        action.handle(request);

        verify(handler).process(event);
        verify(httpAdapter, never()).handleException(any(RestException.class), any(RestRequest.class));
        verify(message, never());
    }

    @Test
    public void actionNotAuthorized() throws RestException, MuleException
    {
        when(httpAdapter.getOperationType()).thenReturn(RestOperationType.RETRIEVE);

        action.setAccessExpression("#[false]");
        when(expressionManager.evaluateBoolean("#[false]", event)).thenReturn(Boolean.FALSE);

        try
        {
            action.handle(request);
        }
        catch (RestException re)
        {
            assertEquals(UnauthorizedException.class, re.getClass());
            verify(handler, never()).process(event);
        }
    }

    // Response representation mediaTypes (as defined in the "Accept" request header)

    @Test
    public void singleAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        when(httpAdapter.getAcceptableResponseMediaTypes()).thenReturn(Collections.singletonList(MediaType.PLAIN_TEXT_UTF_8));
        when(httpAdapter.getRequestMediaType()).thenReturn(MediaType.PLAIN_TEXT_UTF_8);
        action.setRepresentations(Collections.<RepresentationMetaData>singletonList(new DefaultRepresentationMetaData(MediaType.PLAIN_TEXT_UTF_8)));
        action.handle(request);
    }

    @Test
    public void singleNotAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        when(httpAdapter.getAcceptableResponseMediaTypes()).thenReturn(Collections.singletonList(MediaType.PLAIN_TEXT_UTF_8));
        when(httpAdapter.getRequestMediaType()).thenReturn(MediaType.PLAIN_TEXT_UTF_8);
        action.setRepresentations(Collections.<RepresentationMetaData>singletonList(new DefaultRepresentationMetaData(MediaType.PLAIN_TEXT_UTF_8)));
        action.handle(request);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        when(httpAdapter.getAcceptableResponseMediaTypes()).thenReturn(
                Arrays.asList(MediaType.PLAIN_TEXT_UTF_8, MediaType.HTML_UTF_8));
        when(httpAdapter.getRequestMediaType()).thenReturn(MediaType.PLAIN_TEXT_UTF_8);
        action.setRepresentations(Collections.<RepresentationMetaData>singletonList(new DefaultRepresentationMetaData(MediaType.PLAIN_TEXT_UTF_8)));
        action.handle(request);
    }

    @Test
    public void singleAcceptableResponseMediaTypeMultipleMediaTypesSupported()
    {
        fail("Not yet implemented");
    }

    @Test
    public void singleNotAcceptableResponseMediaTypeMultipleMediaTypesSupported()
    {
        fail("Not yet implemented");
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupported()
    {
        fail("Not yet implemented");
    }

    @Test
    public void multipleNotAcceptableResponseMediaTypeMultipleMediaTypesSupported()
    {
        fail("Not yet implemented");
    }

    // Request representation mediaTypes (as defined in the "Content-Type" request header)

    @Test
    public void unsupportedRequestMediaType()
    {
        fail("Not yet implemented");
    }

    // MediaType inheritance from resource

    @Test
    public void mediaTypeInheritedFromResorce()
    {
        fail("Not yet implemented");
    }

    // Defaults

    @Test
    public void defaultMediaType()
    {
        fail("Not yet implemented");
    }

    static class DummyRestAction extends AbstractRestOperation
    {

        @Override
        protected RepresentationMetaData validateAcceptableResponeMediaType(RestRequest request)
            throws MediaTypeNotAcceptableException
        {
            // no response representation needed
            return null;
        }
    }

}
