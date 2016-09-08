/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

public class LeaguesHttpListenerNonBlockingTestCase extends LeaguesHttpListenerTestCase
{

    @Override
    public String isNonBlocking()
    {
        return "true";
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/leagues/leagues-base-flow-config.xml, org/mule/module/apikit/leagues/leagues-http-listener-nb-flow-config.xml";
    }

}
