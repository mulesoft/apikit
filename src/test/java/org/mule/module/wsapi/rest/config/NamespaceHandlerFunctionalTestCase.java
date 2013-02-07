/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.endpoint.DefaultInboundEndpoint;
import org.mule.module.wsapi.rest.RestWebService;
import org.mule.module.wsapi.rest.RestWebServiceInterface;
import org.mule.tck.junit4.FunctionalTestCase;

import org.junit.Test;

public class NamespaceHandlerFunctionalTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/wsapi/rest/config/namespace-handler-config.xml, org/mule/module/wsapi/test-flows-config.xml";
    }

    @Test
    public void testService() throws Exception
    {
        RestWebService webService = muleContext.getRegistry().lookupObject("myService");

        assertNotNull(webService);
        assertEquals(RestWebService.class, webService.getClass());
        assertEquals("myService", webService.getName());
        assertEquals("This is a service", webService.getDescription());
        assertEquals(DefaultInboundEndpoint.class, webService.getMessageSource().getClass());

    }

    @Test
    public void testInterface() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        assertNotNull(wsInterface);
        assertEquals(RestWebServiceInterface.class, wsInterface.getClass());
        assertEquals("myInterface", wsInterface.getName());
        assertEquals("This is an interface", wsInterface.getDescription());
        assertEquals(2, wsInterface.getRoutes().size());
    }

    @Test
    public void testServiceInterfaceReference() throws Exception
    {
        RestWebService webService = muleContext.getRegistry().lookupObject("myService");
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        assertEquals(wsInterface, webService.getInterface());
    }

}
