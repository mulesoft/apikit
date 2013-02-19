/*
 * $Id$
 * --------------------------------------------------------------------------------------
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
import org.mule.module.apikit.rest.action.ActionType;
import org.mule.module.apikit.rest.action.RestAction;
import org.mule.module.apikit.rest.resource.DocumentResource;
import org.mule.module.apikit.rest.resource.RestResource;
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
    public void testInterfaceResourceA() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        RestResource resourceA = (RestResource) wsInterface.getRoutes().get(0);
        assertEquals(DocumentResource.class, resourceA.getClass());
        assertEquals("a", resourceA.getName());
        assertEquals("resource a description", resourceA.getDescription());
        assertEquals("#[true]", resourceA.getAccessExpression());

        assertEquals(1, resourceA.getActions().size());

        RestAction action1 = (RestAction) resourceA.getActions().get(0);
        assertEquals(ActionType.RETRIEVE, action1.getType());
        assertEquals("resource a retrieve description", action1.getDescription());
        assertEquals("#[true]", action1.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action1.getHandler());
    }

    @Test
    public void testInterfaceResourceB() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        RestResource resourceB = (RestResource) wsInterface.getRoutes().get(1);
        assertEquals(DocumentResource.class, resourceB.getClass());
        assertEquals("b", resourceB.getName());
        assertEquals("resource b description", resourceB.getDescription());

        RestAction action1 = (RestAction) resourceB.getActions().get(0);
        assertEquals(ActionType.RETRIEVE, action1.getType());
        assertEquals("resource b retrieve description", action1.getDescription());
        assertEquals("#[true]", action1.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action1.getHandler());

        RestAction action2 = (RestAction) resourceB.getActions().get(1);
        assertEquals(ActionType.UPDATE, action2.getType());
        assertEquals("resource b update description", action2.getDescription());
        assertEquals("#[true]", action2.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action2.getHandler());

        assertEquals(1, ((DocumentResource) resourceB).getResources().size());
        RestResource nestedResource = ((DocumentResource) resourceB).getResources().get(0);
        assertEquals(DocumentResource.class, nestedResource.getClass());
        assertEquals("c", nestedResource.getName());
        assertEquals("resource c description", nestedResource.getDescription());

        RestAction action3 = (RestAction) nestedResource.getActions().get(0);
        assertEquals(ActionType.RETRIEVE, action3.getType());
        assertEquals("resource c retrieve description", action3.getDescription());
        assertEquals("#[true]", action3.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action3.getHandler());

        RestAction action4 = (RestAction) nestedResource.getActions().get(1);
        assertEquals(ActionType.UPDATE, action4.getType());
        assertEquals("resource c update description", action4.getDescription());
        assertEquals("#[true]", action4.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action4.getHandler());
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
