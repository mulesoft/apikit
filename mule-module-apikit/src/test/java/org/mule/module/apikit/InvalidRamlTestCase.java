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

import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.util.IOUtils;

import java.io.InputStream;

import org.junit.Test;
import org.raml.parser.loader.DefaultResourceLoader;

public class InvalidRamlTestCase
{

    @Test
    public void invalidRaml() throws Exception
    {

        Router router = new Router();
        router.setConfig(new Configuration());
        try
        {
            router.validateRaml(getRaml("org/mule/module/apikit/invalid-config.yaml"), new DefaultResourceLoader());
            fail();
        }
        catch (ApikitRuntimeException e)
        {
            assertThat(e.getMessage(), containsString("errors found: 2"));
        }
    }

    private String getRaml(String resource)
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        return IOUtils.toString(stream);
    }
}
