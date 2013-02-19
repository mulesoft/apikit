/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.action;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.apikit.api.Representation;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.protocol.http.HttpRestProtocolAdapter;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class RestActionTestCase extends AbstractMuleTestCase
{
    @Mock
    protected MuleEvent event;
    @Mock
    protected MuleMessage message;
    @Mock
    protected RestRequest request;
    @Mock
    protected HttpRestProtocolAdapter httpAdapter;
    AbstractRestAction action = new DummyRestAction();

    @Before
    public void setup()
    {
        when(event.getMessage()).thenReturn(message);
        doCallRealMethod().when(httpAdapter).handleException(any(RestException.class), any(MuleEvent.class));
        when(request.getProtocolAdaptor()).thenReturn(httpAdapter);
        when(request.getMuleEvent()).thenReturn(event);
    }

    // Response representation mediaTypes (as defined in the "Accept" request header)

    @Test
    public void singleAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        when(httpAdapter.getAcceptedContentTypes()).thenReturn("text/plain");
        when(httpAdapter.getRequestContentType()).thenReturn("text/plain");
        action.setRepresentation(new Representation()
        {

            @Override
            public String getSchemaType()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSchemaLocation()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BigDecimal getQuality()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getMediaType()
            {
                return "text/plain";
            }
        });
        action.handle(request);
    }

    @Test
    public void singleNotAcceptableResponseMediaTypeSingleMediaTypeSupported() throws RestException
    {
        when(httpAdapter.getAcceptedContentTypes()).thenReturn("text/html");
        when(httpAdapter.getRequestContentType()).thenReturn("text/plain");
        action.setRepresentation(new Representation()
        {

            @Override
            public String getSchemaType()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSchemaLocation()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public BigDecimal getQuality()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getMediaType()
            {
                return "text/plain";
            }
        });
        action.handle(request);
    }

    @Test
    public void multipleAcceptableResponseMediaTypeSingleMediaTypeSupported()
    {
        fail("Not yet implemented");
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

    static class DummyRestAction extends AbstractRestAction
    {
    }

}
