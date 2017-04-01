/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

public class RamlHandlerTestCase
{
    @Test
    public void isParserV2FalseUsingRaml08()
    {
        String ramlLocation = this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple08.raml").toString();
        String apiServer = "unused";
        boolean keepRamlBaseUri = true;
        String appHome = "unused";
        RamlHandler handler = new RamlHandler(ramlLocation, apiServer, keepRamlBaseUri, appHome);
        assertTrue(!handler.isParserV2());
    }

    @Test
    public void isParserV2TrueUsingRaml10()
    {
        String ramlLocation = this.getClass().getResource("../../../../org/mule/module/apikit/simple-raml/simple10.raml").toString();
        String apiServer = "unused";
        boolean keepRamlBaseUri = true;
        String appHome = "unused";
        RamlHandler handler = new RamlHandler(ramlLocation, apiServer, keepRamlBaseUri, appHome);
        assertTrue(handler.isParserV2());
    }
}
