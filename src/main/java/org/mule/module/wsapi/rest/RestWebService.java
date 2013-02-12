/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.AbstractWebService;
import org.mule.module.wsapi.rest.resource.BaseResource;
import org.mule.module.wsapi.rest.resource.RestResource;

import java.util.List;

public class RestWebService extends AbstractWebService<RestWebServiceInterface>
{

    public RestWebService(String name, RestWebServiceInterface webServiceInterface, MuleContext muleContext)
    {
        super(name, webServiceInterface, muleContext);
    }

    @Override
    public String getConstructType()
    {
        return "REST-WEB-SERVICE";
    }

    @Override
    protected MessageProcessor getRequestRouter()
    {
        final RestResourceRouter handler = new RestResourceRouter();
        handler.getResources().addAll((List<RestResource>) webServiceInterface.getRoutes());
        handler.getResources().add(new BaseResource(this));

        return new MessageProcessor()
        {
            @Override
            public MuleEvent process(MuleEvent event) throws MuleException
            {
                return handler.handle(new DefaultRestRequest(event));
            }
        };

    }

}
