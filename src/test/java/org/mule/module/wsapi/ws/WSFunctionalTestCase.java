/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.ws;

import org.mule.api.MuleException;
import org.mule.tck.junit4.FunctionalTestCase;

import org.junit.Test;

public class WSFunctionalTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/wsapi/ws/config/ws-namespace-config.xml, org/mule/wsapi/echo-config.xml";
    }

    @Test
    public void testEcho() throws MuleException
    {
        System.out.println(muleContext.getClient().send("http://localhost:8080", "anyone there?", null));
    }

}
