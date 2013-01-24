/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.webservice.rest;

import org.mule.api.MuleEvent;

import java.net.URI;
import java.util.Map;

public class HTTPReSTProtocolAdaptor implements ReSTProtocolAdaptor
{
    protected ResourceOperationType actionType;
    protected URI resourceURI;
    
    public HTTPReSTProtocolAdaptor(MuleEvent event)
    {
    }

    @Override
    public ResourceOperationType getActionType()
    {
        return actionType;
    }

    @Override
    public URI getURI()
    {
        return resourceURI;
    }

    @Override
    public String getAcceptedContentTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestContentType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getQueryParameters()
    {
        // TODO Auto-generated method stub
        return null;
    }

}


