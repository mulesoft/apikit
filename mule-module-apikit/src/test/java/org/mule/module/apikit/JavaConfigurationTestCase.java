/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.mule.api.MuleContext;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.LoggerMessageProcessor;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class JavaConfigurationTestCase extends AbstractMuleTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    private MuleContext muleContext;

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Before
    public void setUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        muleContext = new DefaultMuleContextFactory().createMuleContext();
    }

    @After
    public void tearDown() throws Exception
    {
        muleContext.dispose();
    }

    @Test
    public void runJavaConfig()
    {
        buildJavaConfig(createEndpoint());
        given().header("Accept", "*/*")
                .expect()
                .response().body(CoreMatchers.containsString("/api/leagues"))
                .statusCode(200)
                .when().get("/api/leagues");
    }

    @Test
    public void runJavaConfigNoEndpoint()
    {
        try
        {
            buildJavaConfig(null);
            assertTrue("Expected exception org.mule.api.lifecycle.LifecycleException was not thrown", false);
        }
        catch (Exception e)
        {
            assertThat(e, IsInstanceOf.instanceOf(RuntimeException.class));
            assertThat(e.getMessage(), is("org.mule.api.lifecycle.LifecycleException: Flow endpoint is null, APIKIT requires a listener ref in each of it's flows"));
        }
    }

    private void buildJavaConfig(InboundEndpoint inboundEndpoint)
    {
        try
        {
            //http endpoint
            if (inboundEndpoint != null)
            {
                muleContext.getRegistry().registerEndpoint(inboundEndpoint);
            }

            //action-resource flow
            final Flow flow = new Flow("get:/leagues", muleContext);
            LoggerMessageProcessor loggerMessageProcessor = new LoggerMessageProcessor();
            loggerMessageProcessor.setMessage("Payload is #[payload]");
            flow.setMessageProcessors(Collections.<MessageProcessor>singletonList(loggerMessageProcessor));
            muleContext.getRegistry().registerFlowConstruct(flow);

            //gateway flow
            Flow routerFlow = new Flow("RestRouterFlow", muleContext);
            routerFlow.setMessageSource(inboundEndpoint);
            final Router apikitRouter = configureApikitRouter(muleContext);
            routerFlow.setMessageProcessors(Arrays.<MessageProcessor>asList(apikitRouter));
            muleContext.getRegistry().registerFlowConstruct(routerFlow);

            //start app
            muleContext.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private InboundEndpoint createEndpoint()
    {
        String host = "localhost";
        String path = "api";

        EndpointURIEndpointBuilder endpointURIEndpointBuilder = new EndpointURIEndpointBuilder(
                "http://" + host + ":" + serverPort.getValue() + "/" + path, muleContext);
        try
        {
            return endpointURIEndpointBuilder.buildInboundEndpoint();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }
    private Router configureApikitRouter(MuleContext muleContext) throws IllegalAccessException, InvocationTargetException, InitialisationException
    {
        final Router apikitRouter = new Router();
        final Configuration config = new Configuration();
        config.setRaml("org/mule/module/apikit/leagues/leagues.yaml");
        config.setMuleContext(muleContext);
        config.initialise();
        apikitRouter.setConfig(config);
        apikitRouter.setMuleContext(muleContext);
        return apikitRouter;
    }
}
