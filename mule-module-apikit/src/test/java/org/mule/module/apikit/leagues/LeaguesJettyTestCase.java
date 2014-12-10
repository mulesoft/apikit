/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Ignore;
import org.junit.Rule;

@Ignore //MULE-8123
public class LeaguesJettyTestCase extends LeaguesTestCase
{

    @Rule
    public SystemProperty p1 = new SystemProperty("mule.message.assertAccess", "false");
    @Rule
    public SystemProperty p2 = new SystemProperty("mule.streaming.bufferSize", "32768");
    @Rule
    public SystemProperty p3 = new SystemProperty("mule.transport.tcp.defaultSendTcpNoDelay", "false");
    @Rule
    public SystemProperty p4 = new SystemProperty("mule.transport.http.disableHttpClientStaleConnectionCheck", "true");
    @Rule
    public SystemProperty p5 = new SystemProperty("mule.transport.http.singleDispatcherPerEndpoint", "true");
    @Rule
    public SystemProperty p6 = new SystemProperty("mule.transport.jetty.defaultJettyConnectorClass", "org.eclipse.jetty.server.nio.BlockingChannelConnector");

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/leagues/leagues-base-flow-config.xml, org/mule/module/apikit/leagues/leagues-jetty-flow-config.xml";
    }

}
