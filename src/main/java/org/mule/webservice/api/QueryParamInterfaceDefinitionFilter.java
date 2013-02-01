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
    private WebService webService;

    public QueryParamInterfaceDefinitionFilter(String representionPattern, WebService webService)
    {
        this.representionPattern = representionPattern;
        this.webService = webService;
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
                Definition wsdl = wsdlFactory.newDefinition();
                PortType portType = wsdl.createPortType();
                portType.setQName(new QName(webService.getInterface().getName()));
                wsdl.addPortType(portType);

                for (WebServiceRoute route : webService.getInterface().getRoutes())
                {
                    WSDLOperation wsdlOperation = (WSDLOperation) route;
                    Operation operation = wsdl.createOperation();
                    operation.setName(wsdlOperation.getName());
                    operation.setUndefined(false);
                    portType.addOperation(operation);
                    portType.setUndefined(false);
                }

                WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
                Document wsdlDocument = wsdlWriter.getDocument(wsdl);
                MuleMessage message = new DefaultMuleMessage(wsdlDocument, muleContext);
                return new DefaultMuleEvent(message, event);
            }
            catch (WSDLException e)
            {
                return null;
            }
            catch (Exception e)
            {
                return null;
            }
        }
        else
        {
            return processNext(event);
        }
    }

}
