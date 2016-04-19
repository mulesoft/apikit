/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.construct.Flow;
import org.mule.module.apikit.Configuration;
import org.mule.module.http.internal.listener.DefaultHttpListener;
import org.mule.module.http.internal.listener.DefaultHttpListenerConfig;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BaseUriParametersTestCase extends AbstractMuleContextTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    private Configuration config;

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Before
    public void setupConfig() throws InitialisationException
    {
        DefaultHttpListenerConfig listenerConfig = mock(DefaultHttpListenerConfig.class);
        when(listenerConfig.getHost()).thenReturn("localhost");
        when(listenerConfig.getPort()).thenReturn(8080);
        when(listenerConfig.getTlsContext()).thenReturn(null);
        DefaultHttpListener listener = mock(DefaultHttpListener.class);
        when(listener.getConfig()).thenReturn(listenerConfig);
        when(listener.getPath()).thenReturn("api");
        Flow flow = mock(Flow.class);
        when(flow.getMessageSource()).thenReturn(listener);

        config = new Configuration();
        config.setMuleContext(muleContext);
        config.setRaml("org/mule/module/apikit/parameters/baseuri-parameters.yaml");
        config.initialise();
        config.loadApiDefinition(flow);
    }

    @Test
    public void noBaseUriParameters() throws Exception
    {
        IRaml api = config.getApi();
        assertThat(api.getBaseUriParameters().size(), is(0));
        assertThat(api.getResource("/resources").getBaseUriParameters().size(), is(0));
        assertThat(api.getResource("/resources").getAction("get").getBaseUriParameters().size(), is(0));
    }

}
