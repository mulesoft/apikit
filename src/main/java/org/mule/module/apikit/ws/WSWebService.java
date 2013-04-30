/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.ws;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.processor.MessageProcessorChainBuilder;
import org.mule.module.apikit.AbstractWebService;
import org.mule.module.apikit.api.QueryParamInterfaceDefinitionFilter;

public class WSWebService extends AbstractWebService<WSWebServiceInterface>
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
    protected void configurePreProcessors(MessageProcessorChainBuilder builder) throws MuleException
    {
        super.configurePreProcessors(builder);
        builder.chain(new QueryParamInterfaceDefinitionFilter("?wsdl", this));
    }

    @Override
    public MessageProcessor getRequestRouter()
    {
        if (getInterface().getOperationResolutionMode() == WSOperationResolutionMode.SOAP_ACTION)
        {
            return new SOAPActionOperationRouter(getInterface());
        }
        else if (getInterface().getOperationResolutionMode() == WSOperationResolutionMode.PATH)
        {
            return null;
        }
        else
        {
            return null;
        }
    }

}
