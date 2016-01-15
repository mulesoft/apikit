/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.Pipeline;
import org.mule.api.processor.DefaultMessageProcessorPathElement;
import org.mule.api.processor.MessageProcessor;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.util.NotificationUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test Message processor path are generated correctly.
 */
public class MessageProcessorNotificationPathTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/path/message-processor-path.xml";
    }

    @Test
    public void exceptionStrategy() throws Exception
    {
        testFlowPaths("exception-strategy", "/0", "es/0/0");
    }

    private void testFlowPaths(String flowName, String... nodes) throws Exception
    {
        String[] expectedPaths = generatePaths(flowName, nodes);
        FlowConstruct flow = getFlowConstruct(unescape(flowName));
        DefaultMessageProcessorPathElement flowElement = new DefaultMessageProcessorPathElement(null, flowName);
        ((Pipeline) flow).addMessageProcessorPathElements(flowElement);
        Map<MessageProcessor, String> messageProcessorPaths = NotificationUtils.buildPathResolver(flowElement).getFlowMap();
        String[] flowPaths = messageProcessorPaths.values().toArray(new String[]{});
        Arrays.sort(expectedPaths);
        Arrays.sort(flowPaths);
        Assert.assertArrayEquals(expectedPaths, flowPaths);
    }

    private String[] generatePaths(String flowName, String[] nodes)
    {
        Set<String> pathSet = new LinkedHashSet<String>();
        String base = "/" + flowName + "/processors";
        for (String node : nodes)
        {
            if (!node.startsWith("/"))
            {
                base = "/" + flowName + "/";
            }
            pathSet.add(base + node);
        }
        return pathSet.toArray(new String[0]);
    }

    private String unescape(String name)
    {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            if (i < (name.length() - 1) && name.charAt(i + 1) == '/')
            {
                builder.append("/");
                i++;
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}