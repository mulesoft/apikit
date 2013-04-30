/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.ws;

import org.mule.tck.junit4.FunctionalTestCase;

import java.util.Collections;

import org.junit.Test;

public class WSFunctionalTestCase extends FunctionalTestCase
{

    private static String EMPTY_SOAP_ENVELOPE = "<?xml version='1.0' encoding='utf-8'?><soap:velope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
                                                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                                                + "<soap:Body><echo/></soap:Body></soap:velope>";

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/ws/service-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void testWSDL() throws Exception
    {
        System.out.println(muleContext.getClient().send("http://localhost:8080/ws/?wsdl", EMPTY_SOAP_ENVELOPE,
            Collections.singletonMap("SOAPAction", (Object) "hello")).getPayloadAsString());
    }

    @Test
    public void testHelloWorldOperation() throws Exception
    {
        System.out.println(muleContext.getClient()
            .send("http://localhost:8080/ws/", EMPTY_SOAP_ENVELOPE,
                Collections.singletonMap("SOAPAction", (Object) "hello"))
            .getPayloadAsString());
    }

    @Test
    public void testNotFoundOperation() throws Exception
    {
        System.out.println(muleContext.getClient()
            .send("http://localhost:8080/ws/", EMPTY_SOAP_ENVELOPE,
                Collections.singletonMap("SOAPAction", (Object) "notFound"))
            .getPayloadAsString());
    }

    
}
