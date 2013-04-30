/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.module.apikit.rest.RestWebServiceInterface;
import org.mule.tck.junit4.FunctionalTestCase;

import org.junit.Test;

public class InterfaceConfigurationTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/config/configuration-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void testInterface() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        assertNotNull(wsInterface);
        assertEquals(RestWebServiceInterface.class, wsInterface.getClass());
        assertEquals("myInterface", wsInterface.getName());
        assertEquals("interface description", wsInterface.getDescription());
        assertEquals(2, wsInterface.getRoutes().size());
    }

    @Test
    public void testInterface2() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface2");

        assertNotNull(wsInterface);
        assertEquals(RestWebServiceInterface.class, wsInterface.getClass());
        assertEquals("myInterface2", wsInterface.getName());
        assertEquals("interface description", wsInterface.getDescription());
        assertEquals(2, wsInterface.getRoutes().size());
    }
}
