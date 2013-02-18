/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.ws;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.module.apikit.AsbtarctWebServiceOperationRouter;
import org.mule.module.apikit.api.WebServiceInterface;

import java.util.List;

public class SOAPActionOperationRouter extends AsbtarctWebServiceOperationRouter
{

    public SOAPActionOperationRouter(WebServiceInterface webServiceInterface)
    {
        super(webServiceInterface);
    }

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        String soapAction = event.getMessage().getInboundProperty("SOAPAction");
        for (WSDLOperation route : ((List<WSDLOperation>) webServiceInterface.getRoutes()))
        {
            if (route.getName().equals(soapAction))
            {
                return new DefaultMuleEvent(new DefaultMuleMessage("<soap:Envelope><soap:Body>"
                                                                   + ((WSDLOperation) route).process(event)
                                                                       .getMessageAsString()
                                                                   + "</soap:Body></soap:Envelope>",
                    event.getMuleContext()), event);
            }
        }
        return new DefaultMuleEvent(new DefaultMuleMessage(
            "<soap:Envelope><soap:Fault>NO OPERATION FOUND</soap:Fault></soap:Envelope>",
            event.getMuleContext()), event);
    }

}
