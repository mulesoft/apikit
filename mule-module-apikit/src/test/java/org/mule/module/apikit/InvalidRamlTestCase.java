/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Test;

public class InvalidRamlTestCase extends AbstractMuleContextTestCase
{

    @Test
    public void invalidRaml() throws Exception
    {
        try
        {
            validateRaml("org/mule/module/apikit/invalid-config.yaml");
            fail();
        }
        catch (ApikitRuntimeException e)
        {
            assertThat(e.getMessage(), containsString("errors found: 2"));
        }
    }

    private Router validateRaml(String ramlPath) throws InitialisationException
    {
        Configuration config = new Configuration();
        config.setMuleContext(muleContext);
        config.setRaml(ramlPath);
        config.initialise();

        Router router = new Router();
        router.setConfig(config);
        return router;
    }

    @Test
    public void invalidRamlLocation() throws Exception
    {
        try
        {
            validateRaml("invalidRamlLocation.raml");
            fail();
        }
        catch (ApikitRuntimeException e)
        {
            assertThat(e.getMessage(), containsString("errors found: 1"));
            assertThat(e.getMessage(), containsString("RAML resource not found --  file: invalidRamlLocation.raml"));
        }
    }
}
