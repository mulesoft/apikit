/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.ws;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.webservice.api.WebServiceRoute;
import org.mule.webservice.ws.WSDLOperation;

import java.util.Collections;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.w3c.dom.Document;

public class WSFunctionalTestCase extends FunctionalTestCase
{

    private static String EMPTY_SOAP_ENVELOPE = "<?xml version='1.0' encoding='utf-8'?><soap:velope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
                                                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"
                                                + "<soap:Body><echo/></soap:Body></soap:velope>";

    @Override
    protected String getConfigResources()
    {
        return "org/mule/wsapi/ws/ws-functional-test-config.xml, org/mule/wsapi/echo-config.xml";
    }

    @Test
    public void testWSDL() throws Exception
    {
        WSDLFactory wsdlFactory = javax.wsdl.factory.WSDLFactory.newInstance();
        Definition wsdl =  wsdlFactory.newDefinition();
        PortType portType = wsdl.createPortType();
        portType.setQName(new QName("op1"));
        wsdl.addPortType(portType);

            Operation operation = wsdl.createOperation();
            operation.setName("op1");
            portType.addOperation(operation);
        WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
        Document wsdlDocument = wsdlWriter.getDocument(wsdl);
        MuleMessage message = new DefaultMuleMessage(wsdlDocument, muleContext);
        System.out.println(message.getPayloadAsString());

        
        
        
//        System.out.println(muleContext.getClient().send("http://localhost:8080/ws/?wsdl", EMPTY_SOAP_ENVELOPE,
//            Collections.singletonMap("SOAPAction", (Object) "hello")).getPayloadAsString());
//    }
//
//    @Test
//    public void testHelloWorldOperation() throws Exception
//    {
//        System.out.println(muleContext.getClient().send("http://localhost:8080/ws/", EMPTY_SOAP_ENVELOPE,
//            Collections.singletonMap("SOAPAction", (Object) "hello")).getPayloadAsString());
    }

}
