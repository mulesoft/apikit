/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.context.notification.ServerNotificationManager;
import org.mule.management.stats.AllStatistics;
import org.mule.tck.TriggerableMessageSource;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RestWebServiceTestCase extends AbstractMuleTestCase
{

    protected RestWebService restWebService;
    protected MuleContext muleContext = Mockito.mock(MuleContext.class);
    protected RestWebServiceInterface interface1 = Mockito.mock(RestWebServiceInterface.class);
    protected TriggerableMessageSource trigger = new TriggerableMessageSource();
    protected MuleEvent event = Mockito.mock(MuleEvent.class);
    protected MuleMessage muleMessage = Mockito.mock(MuleMessage.class);

    @Before
    public void setup() throws URISyntaxException
    {
        Mockito.when(muleContext.getStatistics()).thenReturn(new AllStatistics());
        ServerNotificationManager notificationManager = new ServerNotificationManager();
        notificationManager.setMuleContext(muleContext);
        Mockito.when(muleContext.getNotificationManager()).thenReturn(notificationManager);
        Mockito.when(muleContext.getConfiguration()).thenReturn(new DefaultMuleConfiguration());
        Mockito.when(event.getSession()).thenReturn(Mockito.mock(MuleSession.class));
        Mockito.when(event.getMessageSourceURI()).thenReturn(new URI("http://localhost:8080/"));
        Mockito.when(event.getMessage()).thenReturn(muleMessage);
        Mockito.when(event.getMessage().getInboundProperty("http.request.path")).thenReturn("/api/1/2");
        Mockito.when(muleMessage.getInboundProperty("http.method")).thenReturn("GET");
        restWebService = new RestWebService("name", interface1, false, muleContext);
        restWebService.setMessageSource(trigger);
    }

    @Test(expected = InitialisationException.class)
    public void atLeastOneResource() throws MuleException
    {
        Mockito.when(interface1.getRoutes()).thenReturn(new ArrayList());

        restWebService.initialise();
    }

}
