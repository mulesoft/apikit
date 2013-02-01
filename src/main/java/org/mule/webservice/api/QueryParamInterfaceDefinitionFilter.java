/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.api;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.webservice.ws.WSDLOperation;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

public class QueryParamInterfaceDefinitionFilter extends AbstractInterceptingMessageProcessor
{

    private String representionPattern;
    private WebServiceInterface webServiceInterface;

    public QueryParamInterfaceDefinitionFilter(String representionPattern, WebServiceInterface webServiceInterface)
    {
        this.representionPattern = representionPattern;
        this.webServiceInterface = webServiceInterface;
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        String requestUri = event.getMessage().getInboundProperty("http.request");
        if (requestUri.endsWith(representionPattern))
        {
            try
            {
                WSDLFactory wsdlFactory = javax.wsdl.factory.WSDLFactory.newInstance();
                Definition wsdl =  wsdlFactory.newDefinition();
                PortType portType = wsdl.createPortType();
                portType.setQName(new QName(webServiceInterface.getName()));
                wsdl.addPortType(portType);

                for (WebServiceRoute route : webServiceInterface.getRoutes())
                {
                    WSDLOperation wsdlOperation = (WSDLOperation) route;
                    Operation operation = wsdl.createOperation();
                    operation.setName(wsdlOperation.getName());
                    portType.addOperation(operation);
                }
                
                WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
                Document wsdlDocument = wsdlWriter.getDocument(wsdl);
                MuleMessage message = new DefaultMuleMessage(wsdlDocument, muleContext);
                System.out.println(message.getPayloadAsString());
                
                return new DefaultMuleEvent(message, event);
            }
            catch (WSDLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }   
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            
            
        }
        else
        {   
            return processNext(event);
        }
        return event;
    }

}
