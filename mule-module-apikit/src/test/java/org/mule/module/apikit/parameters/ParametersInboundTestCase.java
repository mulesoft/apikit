/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parameters;

public class ParametersInboundTestCase extends ParametersTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/parameters/parameters-config-inbound.xml";
    }

    @Override
    public void repeatableHeader() {
        System.out.println("Not supported - Test skipped");
    }
}
