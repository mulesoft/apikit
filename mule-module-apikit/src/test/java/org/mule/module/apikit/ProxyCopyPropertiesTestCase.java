/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.junit.Assert.assertEquals;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.transport.PropertyScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

public class ProxyCopyPropertiesTestCase
{
    @Test
    public void copyPropertiesTest()
    {
        MuleEvent event = Mockito.mock(MuleEvent.class);
        MuleContext context = Mockito.mock(MuleContext.class);
        MuleConfiguration muleConfiguration = Mockito.mock(MuleConfiguration.class);
        Mockito.when(context.getConfiguration()).thenReturn(muleConfiguration);
        Mockito.when(context.getConfiguration().getDefaultEncoding()).thenReturn("UTF-8");
        Map<String, Object> inboundProperties = new HashMap<>();
        Map<String, Object> outboundProperties = new HashMap<>();
        inboundProperties.put("headerToRemoveByWildcard","value");
        inboundProperties.put("remove-it","value");
        inboundProperties.put("dont-remove-it","value");
        MuleMessage message = new DefaultMuleMessage(null, inboundProperties, outboundProperties, null, context);
        Map<String,Object> invocationProperties = new HashMap<>();
        List<String> headersToIgnore = new ArrayList<>();
        headersToIgnore.add("header*");
        headersToIgnore.add("remove-it");
        invocationProperties.put("_headersToIgnore",headersToIgnore);
        message.addProperties(invocationProperties, PropertyScope.INVOCATION);
        Mockito.when(event.getMessage()).thenReturn(message);
        Set<String> skip = new HashSet<>();
        Proxy.copyProperties(event, skip);
        Set<String> outboundPropertiesResult = event.getMessage().getOutboundPropertyNames();
        assertEquals(1,outboundPropertiesResult.size());
        assertEquals("dont-remove-it", outboundPropertiesResult.iterator().next());
    }
}
