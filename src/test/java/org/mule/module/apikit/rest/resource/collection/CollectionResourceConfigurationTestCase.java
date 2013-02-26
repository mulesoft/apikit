/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.mule.module.apikit.rest.RestWebServiceInterface;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.document.DocumentResource;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.Rule;
import org.junit.Test;

public class CollectionResourceConfigurationTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/resource/collection/collection-configuration-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void testCollectionResourceA() throws Exception
    {
        RestWebServiceInterface wsInterface = muleContext.getRegistry().lookupObject("myInterface");

        CollectionResource resourceA = (CollectionResource) wsInterface.getRoutes().get(0);
        assertEquals("a", resourceA.getName());
        assertEquals("resource a description", resourceA.getDescription());
        assertEquals("#[true]", resourceA.getAccessExpression());

        RestOperation action1 = (RestOperation) resourceA.getOperations().get(0);
        assertEquals(RestOperationType.RETRIEVE, action1.getType());
        assertEquals("resource a retrieve description", action1.getDescription());
        assertEquals("#[true]", action1.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action1.getHandler());

        assertNotNull(resourceA.getMemberResource());
        CollectionMemberResource member = resourceA.getMemberResource();
        assertEquals(resourceA.getName() + "Member", member.getName());
        assertEquals("collection member description", member.getDescription());
        assertNull(member.getAccessExpression());
        assertEquals(4, member.getOperations().size());
        assertEquals(1, resourceA.getOperations().size());
        assertEquals(2, member.getRepresentations().size());

        RestOperation action2 = (RestOperation) member.getOperations().get(0);
        assertEquals(RestOperationType.CREATE, action2.getType());
        assertEquals("collection a member create description", action2.getDescription());
        assertEquals("#[true]", action2.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action2.getHandler());

        RestOperation action3 = (RestOperation) member.getOperations().get(1);
        assertEquals(RestOperationType.RETRIEVE, action3.getType());
        assertEquals("collection a member retrieve description", action3.getDescription());
        assertEquals("#[true]", action3.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action3.getHandler());

        RestOperation action4 = (RestOperation) member.getOperations().get(2);
        assertEquals(RestOperationType.UPDATE, action4.getType());
        assertEquals("collection a member update description", action4.getDescription());
        assertEquals("#[true]", action4.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action4.getHandler());

        RestOperation action5 = (RestOperation) member.getOperations().get(3);
        assertEquals(RestOperationType.DELETE, action5.getType());
        assertEquals("collection a member delete description", action5.getDescription());
        assertEquals("#[true]", action5.getAccessExpression());
        assertEquals(muleContext.getRegistry().lookupObject("echo"), action5.getHandler());

        assertEquals(1, member.getResources().size());
        DocumentResource child = (DocumentResource) member.getResources().get(0);
        assertEquals("c", child.getName());
        assertEquals("resource c description", child.getDescription());
        assertEquals("#[true]", child.getAccessExpression());
        assertEquals(2, child.getOperations().size());
        assertEquals(2, child.getRepresentations().size());

    }

}
