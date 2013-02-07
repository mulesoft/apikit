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

import org.mule.api.MuleContext;
import org.mule.module.wsapi.AbstractWebService;
import org.mule.module.wsapi.api.QueryParamInterfaceDefinitionFilter;

public class WSWebService extends AbstractWebService
{

    public WSWebService(String name, WSWebServiceInterface webServiceInterface, MuleContext muleContext)
    {
        super(name, webServiceInterface, muleContext);
    }

    @Override
    public String getConstructType()
    {
        return "WS-WEB-SERVICE";
    }

    @Override
    protected QueryParamInterfaceDefinitionFilter getInterfaceRepresentationFilter()
    {
        return new QueryParamInterfaceDefinitionFilter("?wsdl", this);
    }

}
