/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.ConsoleHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.Test;
import org.mule.raml.interfaces.model.IRaml;

public class ConsoleRamlUriTestCase
{

    private String consoleBaseUri = "http://localhost:8081/console";
    private Configuration configuration = mock(Configuration.class);
    private IRaml api = mock(IRaml.class);
    {
        when(configuration.isParserV2()).thenReturn(true);
        when(configuration.getAppHome()).thenReturn(new File("src/test/resources").getAbsolutePath());
        when(configuration.getApi()).thenReturn(api);
        when(api.getAllReferences()).thenReturn(Collections.<String>emptyList());
    }

    @Test
    public void apiDirectory() throws Exception
    {
        when(configuration.getRaml()).thenReturn("api.raml");
        assertThat(getApiResourcesRelativePath(getConsoleHandler()), is("api/"));
    }

    @Test
    public void nestedApiDirecotry() throws Exception
    {
        when(configuration.getRaml()).thenReturn("subdir/api.raml");
        assertThat(getApiResourcesRelativePath(getConsoleHandler()), is("api/subdir/"));
    }

    private ConsoleHandler getConsoleHandler()
    {
        ConsoleHandler consoleHandler = new ConsoleHandler(consoleBaseUri, configuration);
        consoleHandler.updateRamlUri();
        return consoleHandler;
    }

    private String getApiResourcesRelativePath(ConsoleHandler consoleHandler) throws NoSuchFieldException, IllegalAccessException
    {
        Field f = consoleHandler.getClass().getDeclaredField("apiResourcesRelativePath");
        f.setAccessible(true);
        return (String) f.get(consoleHandler);
    }
}
