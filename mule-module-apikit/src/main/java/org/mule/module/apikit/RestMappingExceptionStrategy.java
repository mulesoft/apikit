/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.transport.http.HttpConnector.HTTP_STATUS_PROPERTY;

import org.mule.api.GlobalNameableObject;
import org.mule.api.MuleEvent;
import org.mule.api.exception.MessagingExceptionHandlerAcceptor;
import org.mule.message.DefaultExceptionPayload;
import org.mule.processor.AbstractMuleObjectOwner;

import java.util.List;

public class RestMappingExceptionStrategy extends AbstractMuleObjectOwner<MappingExceptionListener>
        implements MessagingExceptionHandlerAcceptor, GlobalNameableObject
{

    private List<MappingExceptionListener> exceptionListeners;

    protected String globalName;

    @Override
    public MuleEvent handleException(Exception exception, MuleEvent event)
    {
        event.getMessage().setExceptionPayload(new DefaultExceptionPayload(exception));
        for (MessagingExceptionHandlerAcceptor exceptionListener : exceptionListeners)
        {
            if (exceptionListener.accept(event))
            {
                event.getMessage().setExceptionPayload(null);
                return exceptionListener.handleException(exception, event);
            }
        }
        return defaultHandler(exception, event);
    }

    protected MuleEvent defaultHandler(Exception exception, MuleEvent event)
    {
        String currentStatus = event.getMessage().getOutboundProperty(HTTP_STATUS_PROPERTY);
        //if status already 4xx or 5xx -> pass-through
        if (!isErrorCode(currentStatus))
        {
            event.getMessage().setOutboundProperty(HTTP_STATUS_PROPERTY, 500);
            event.getMessage().setPayload(exception.getMessage());
        }
        return event;
    }

    private boolean isErrorCode(String statusCode)
    {
        if (statusCode == null)
        {
            return false;
        }
        int status = 0;
        try
        {
            status = Integer.parseInt(statusCode);
        }
        catch (NumberFormatException nfe)
        {
            //ignore
        }
        return status >= 400 && status < 600;
    }

    @Override
    public boolean accept(MuleEvent event)
    {
        return true;
    }

    @Override
    public boolean acceptsAll()
    {
        return true;
    }


    public void setExceptionListeners(List<MappingExceptionListener> exceptionListeners)
    {
        this.exceptionListeners = exceptionListeners;
    }

    @Override
    public String getGlobalName()
    {
        return globalName;
    }

    @Override
    public void setGlobalName(String globalName)
    {
        this.globalName = globalName;
    }

    @Override
    protected List<MappingExceptionListener> getOwnedObjects()
    {
        return exceptionListeners;
    }

}
