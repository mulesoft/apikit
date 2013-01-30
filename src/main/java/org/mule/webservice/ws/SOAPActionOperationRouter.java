/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.ws;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.processor.MessageRouter;
import org.mule.webservice.api.WebServiceRoute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SOAPActionOperationRouter implements MessageProcessor
{

    protected Map<String, WebServiceRoute> routes = new HashMap<String, WebServiceRoute>();
    
    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        String soapAction = event.getMessage().getInboundProperty("SOAPAction");
        if(routes.containsKey(soapAction)){
            return routes.get(soapAction).process(event);
        }
        else{
            return null;
        }
    }
    
    void addRoute(String name, WebServiceRoute route) throws MuleException
    {
        routes.put(name, route);
    }

    void removeRoute(WebServiceRoute route) throws MuleException
    {
        routes.remove(route);
    }


}


