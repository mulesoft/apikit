/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.mule.tck.junit4.FunctionalTestCase;

import java.util.Collection;

import org.junit.Test;

public class RouterlessConfigurationTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/config/routerless-config.xml";
    }

    @Test
    public void alive()
    {
        Collection<Configuration> configurations = muleContext.getRegistry().lookupObjects(Configuration.class);
        assertThat(configurations.size(), is(1));
        Configuration config = configurations.iterator().next();
        assertThat(config.getFlowMappings().size(), is(2));
        assertThat(config.getApi(), notNullValue());
        assertThat(config.getRawRestFlowMap().size(), is(3));
        assertThat(config.getRestFlowMap().size(), is(3));
    }
}
