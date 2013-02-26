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

import static com.google.common.net.MediaType.ANY_TYPE;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static com.google.common.net.MediaType.XML_UTF_8;
import static org.junit.Assert.assertEquals;
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
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
    protected RestResource resource;
    @Mock
    RestWebService service;

    AbstractRestOperation action = new DummyRestAction();

    @Before
    public void setup() throws MuleException
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter)
            .handleException(any(RestException.class), any(RestRequest.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
        when(request.getService()).thenReturn(service);
        when(service.getMuleContext()).thenReturn(muleContext);
        when(muleContext.getExpressionManager()).thenReturn(expressionManager);
        when(handler.process(any(MuleEvent.class))).thenAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[0];
            }
        });
        action.setHandler(handler);
        action.setResource(resource);

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
    public void anyAcceptableResponseMediaType() throws RestException
    {
        accept(ANY_TYPE);
        request(PLAIN_TEXT_UTF_8);
        produceOperation(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void singleAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void singleAcceptableResponseMediaTypeSingleMediaTypeSupportedOnResource() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void singleNotAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8);
        expectException();
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void singleNotAcceptableResponseMediaTypeSingleMediaTypeSupportedOnResource() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(XML_UTF_8);
        expectException();
    }

    @Test
    public void multipleAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeSingleMediaTypeSupportedOnResource() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void singleAcceptableResponseMediaTypeMultipleMediaTypesSupported() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void singleAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnResource() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void singleAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnBoth() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void singleNotAcceptableResponseMediaTypeMultipleMediaTypesSupported() throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expectException();
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void singleNotAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnResource()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expectException();
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void singleNotAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnBoth() throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "1"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expectException();
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupported() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnResource()
        throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        request(PLAIN_TEXT_UTF_8);
        produceResource(XML_UTF_8, PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnBoth() throws RestException
    {
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        request(PLAIN_TEXT_UTF_8);
        produceOperation(XML_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expect(PLAIN_TEXT_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQuality() throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.5"), XML_UTF_8.withParameter("q", "0.8"));
        request(XML_UTF_8);
        produceOperation(JSON_UTF_8, XML_UTF_8);
        expect(XML_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQualityOnResource()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.5"), XML_UTF_8.withParameter("q", "0.8"));
        request(XML_UTF_8);
        produceResource(JSON_UTF_8, XML_UTF_8);
        expect(XML_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQualityOnBoth()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.5"), XML_UTF_8.withParameter("q", "0.8"));
        request(XML_UTF_8);
        produceOperation(XML_UTF_8);
        produceResource(PLAIN_TEXT_UTF_8);
        expect(XML_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQualityReversed()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.7"), XML_UTF_8.withParameter("q", "0.3"));
        request(XML_UTF_8);
        produceOperation(JSON_UTF_8, XML_UTF_8);
        expect(JSON_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQualityReversedOnResource()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.7"), XML_UTF_8.withParameter("q", "0.3"));
        request(XML_UTF_8);
        produceResource(JSON_UTF_8, XML_UTF_8);
        expect(JSON_UTF_8);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeMultipleMediaTypesSupportedQualityReversedOnBoth()
        throws RestException
    {
        accept(JSON_UTF_8.withParameter("q", "0.7"), XML_UTF_8.withParameter("q", "0.3"));
        request(XML_UTF_8);
        produceOperation(JSON_UTF_8);
        produceResource(XML_UTF_8);
        expect(JSON_UTF_8);
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void multipleNotAcceptableResponseMediaTypeMultipleMediaTypesSupported() throws RestException
    {
        request(PLAIN_TEXT_UTF_8);
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        produceOperation(XML_UTF_8, JSON_UTF_8);
        expectException();
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void multipleNotAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnResource()
        throws RestException
    {
        request(PLAIN_TEXT_UTF_8);
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        produceResource(XML_UTF_8, JSON_UTF_8);
        expectException();
    }

    @Test(expected = MediaTypeNotAcceptableException.class)
    public void multipleNotAcceptableResponseMediaTypeMultipleMediaTypesSupportedOnBoth()
        throws RestException
    {
        request(PLAIN_TEXT_UTF_8);
        accept(PLAIN_TEXT_UTF_8.withParameter("q", "0.5"), HTML_UTF_8.withParameter("q", "0.5"));
        produceOperation(XML_UTF_8);
        produceResource(JSON_UTF_8);
        expectException();
    }

    private void request(MediaType mediaType)
    {
        when(httpAdapter.getRequestMediaType()).thenReturn(mediaType);
    }

    private void accept(MediaType... mediaTypes)
    {
        if (mediaTypes == null)
        {
            when(httpAdapter.getAcceptableResponseMediaTypes()).thenReturn(null);
        }
        else
        {
            when(httpAdapter.getAcceptableResponseMediaTypes()).thenReturn(Arrays.asList(mediaTypes));
        }
    }

    private void produceOperation(MediaType... mediaTypes)
    {
        List<RepresentationMetaData> representations = new ArrayList<RepresentationMetaData>();
        for (MediaType mediaType : mediaTypes)
        {
            representations.add(new TestRepresentationMetaData(mediaType));
        }
        action.setRepresentations(representations);
    }

    private void produceResource(MediaType... mediaTypes)
    {
        List<RepresentationMetaData> representations = new ArrayList<RepresentationMetaData>();
        for (MediaType mediaType : mediaTypes)
        {
            representations.add(new TestRepresentationMetaData(mediaType));
        }
        when(resource.getRepresentations()).thenReturn(representations);
    }

    private void expect(MediaType mediaType) throws RestException
    {
        action.handle(request);
        MuleEvent muleEvent = request.getMuleEvent();
        ArgumentCaptor<Object> arg = ArgumentCaptor.forClass(Object.class);
        verify(muleEvent.getMessage()).setPayload(arg.capture());
        assertEquals(mediaType.withoutParameters(), ((MediaType) arg.getValue()).withoutParameters());
    }

    private void expectException() throws RestException
    {
        action.handle(request);
    }

    static class TestRepresentationMetaData extends DefaultRepresentationMetaData
    {
        TestRepresentationMetaData(MediaType mediaType)
        {
            super(mediaType);
            if (!mediaType.parameters().containsKey("q"))
            {
                this.mediaType = mediaType.withParameter("q", "1");
            }
        }

        @Override
        public Object toRepresentation(MuleEvent event, RestRequest request)
        {
            return mediaType;
        }
    }

    static class DummyRestAction extends AbstractRestOperation
    {
        @Override
        public RestOperationType getType()
        {
            return RestOperationType.RETRIEVE;
        }
    }

}
