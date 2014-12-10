/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.mule36;

import org.mule.module.apikit.LeaguesTestCase;

//TODO merge with LeaguesTestCase when minimum version supported is 3.6
public class LeaguesMule36TestCase extends LeaguesTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/leagues/leagues-base-flow-config.xml, org/mule/module/apikit/leagues/leagues-mule36-flow-config.xml";
    }

}
