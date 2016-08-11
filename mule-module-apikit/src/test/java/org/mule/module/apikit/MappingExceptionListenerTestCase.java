/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.message.DefaultExceptionPayload;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.exception.MuleRestException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingExceptionListenerTestCase
{

    private MappingExceptionListener listener;

    @Before
    public void setup()
    {
        listener = new MappingExceptionListener();
    }

    @Test
    public void testExceptionListenerAcceptsInitializedException() throws InitialisationException
    {
        Exception exception = new MuleRestException();
        listener.setExceptions(Lists.newArrayList(exception.getClass().getName()));
        listener.setMuleContext(mockMuleContext());
        listener.initialise();

        boolean accepted = listener.accept(mockMuleEventWithException(exception));

        assertTrue(accepted);
    }

    @Test
    public void testExceptionListenerDoesNotAcceptNotInitializedException() throws InitialisationException
    {
        Exception exception = new MuleRestException();
        listener.setExceptions(Lists.newArrayList(exception.getClass().getName()));
        listener.setMuleContext(mockMuleContext());
        listener.initialise();

        boolean accepted = listener.accept(mockMuleEventWithException(new RuntimeException()));

        assertFalse(accepted);
    }

    @Test(expected = ApikitRuntimeException.class)
    public void testUnknownExceptionMappingFailsDuringInitialization() throws InitialisationException
    {
        listener.setExceptions(Lists.newArrayList("unknownException"));
        listener.setMuleContext(mockMuleContext());

        listener.initialise();
    }

    private MuleContext mockMuleContext()
    {
        MuleContext muleContext = mock(MuleContext.class);
        when(muleContext.getExecutionClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        return muleContext;
    }

    private MuleEvent mockMuleEventWithException(Exception exception)
    {
        MuleEvent muleEvent = mock(MuleEvent.class);
        MuleMessage muleMessage = mock(MuleMessage.class);
        when(muleEvent.getMessage()).thenReturn(muleMessage);
        when(muleMessage.getExceptionPayload()).thenReturn(new DefaultExceptionPayload(exception));
        return muleEvent;
    }
}
