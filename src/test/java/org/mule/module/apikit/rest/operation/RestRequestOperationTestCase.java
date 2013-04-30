/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.operation;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.XML_UTF_8;
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
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.RestWebService;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class RestRequestOperationTestCase extends AbstractMuleTestCase
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

    // Request representation mediaTypes (as defined in the "Content-Type" request header)

    @Test(expected = UnsupportedMediaTypeException.class)
    public void unsupportedRequestMediaType() throws RestException
    {
        request(JSON_UTF_8);
        receiveOperation(XML_UTF_8);
        expectException();
    }

    @Test(expected = UnsupportedMediaTypeException.class)
    public void unsupportedRequestMediaTypeOnResource() throws RestException
    {
        request(JSON_UTF_8);
        receiveResource(XML_UTF_8);
        expectException();
    }

    @Test
    public void noRequestMediaType() throws RestException
    {
        request(null);
        receiveOperation(XML_UTF_8);
        expectNoResponse();
    }

    @Test
    public void supportedRequestMediaType() throws RestException
    {
        request(XML_UTF_8);
        receiveOperation(XML_UTF_8);
        expectNoResponse();
    }

    // MediaType inheritance from resource

    @Test
    public void supportedRequestMediaTypeOnResource() throws RestException
    {
        request(XML_UTF_8);
        receiveResource(XML_UTF_8);
        expectNoResponse();
    }

    // Defaults

    @Test
    public void defaultMediaType() throws RestException
    {
        request(JSON_UTF_8);
        expectNoResponse();
    }

    private void request(MediaType mediaType)
    {
        when(httpAdapter.getRequestMediaType()).thenReturn(mediaType);
    }

    private void receiveOperation(MediaType... mediaTypes)
    {
        List<RepresentationMetaData> representations = new ArrayList<RepresentationMetaData>();
        for (MediaType mediaType : mediaTypes)
        {
            representations.add(new TestRepresentationMetaData(mediaType));
        }
        action.setRepresentations(representations);
    }

    private void receiveResource(MediaType... mediaTypes)
    {
        List<RepresentationMetaData> representations = new ArrayList<RepresentationMetaData>();
        for (MediaType mediaType : mediaTypes)
        {
            representations.add(new TestRepresentationMetaData(mediaType));
        }
        when(resource.getRepresentations()).thenReturn(representations);
    }

    private void expectNoResponse() throws RestException
    {
        action.handle(request);
        MuleEvent muleEvent = request.getMuleEvent();
        verify(muleEvent.getMessage(), never()).setPayload(any());
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
            return RestOperationType.CREATE;
        }
    }

}
