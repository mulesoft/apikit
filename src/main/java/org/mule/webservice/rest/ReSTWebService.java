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

import org.mule.api.MuleContext;
import org.mule.webservice.AbstractWebService;
import org.mule.webservice.api.InterfaceRepresentationFilter;
import org.mule.webservice.api.WebService;
import org.mule.webservice.api.WebServiceInterface;

public class ReSTWebService extends AbstractWebService<WebServiceInterface<?>> implements WebService
{

    public ReSTWebService(String name, WebServiceInterface<?> webServiceInterface, MuleContext muleContext)
    {
        super(name, webServiceInterface, muleContext);
    }

    @Override
    protected InterfaceRepresentationFilter getInterfaceRepresentationFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
