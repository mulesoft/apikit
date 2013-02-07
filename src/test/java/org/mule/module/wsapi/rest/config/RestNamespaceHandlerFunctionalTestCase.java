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

import org.mule.module.wsapi.api.WebService;
import org.mule.module.wsapi.rest.RestWebService;
import org.mule.module.wsapi.rest.RestWebServiceInterface;
import org.mule.tck.junit4.FunctionalTestCase;

import org.junit.Test;

public class RestNamespaceHandlerFunctionalTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/wsapi/rest/config/rest-namespace-config.xml, org/mule/module/wsapi/rest/flow-config.xml";
    }

    @Test
    public void testInterfaceCreation() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");
        assertNotNull(wsInterface);
        assertEquals(RestWebServiceInterface.class, wsInterface.getClass());
        assertEquals(2, wsInterface.getRoutes().size());
    }

    @Test
    public void testServiceCreation() throws Exception
    {
        assertNotNull(muleContext.getRegistry().lookupObject("myService"));
        assertEquals(RestWebService.class, muleContext.getRegistry()
            .lookupFlowConstruct("myService")
            .getClass());
        assertEquals("myService", muleContext.getRegistry().lookupFlowConstruct("myService").getName());
    }

    @Test
    public void testServiceInterfaceReference() throws Exception
    {
        assertEquals(muleContext.getRegistry().lookupObject("myInterface"),
            ((WebService) muleContext.getRegistry().lookupFlowConstruct("myService")).getInterface());
    }

}
