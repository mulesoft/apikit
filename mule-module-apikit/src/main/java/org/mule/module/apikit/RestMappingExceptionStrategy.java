/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.api.GlobalNameableObject;
import org.mule.api.MuleEvent;
import org.mule.api.exception.MessagingExceptionHandlerAcceptor;
import org.mule.api.processor.MessageProcessorContainer;
import org.mule.api.processor.MessageProcessorPathElement;
import org.mule.message.DefaultExceptionPayload;
import org.mule.processor.AbstractMuleObjectOwner;

import java.util.List;

public class RestMappingExceptionStrategy extends AbstractMuleObjectOwner<MappingExceptionListener>
        implements MessagingExceptionHandlerAcceptor, GlobalNameableObject, MessageProcessorContainer
{

    private List<MappingExceptionListener> exceptionListeners;

    protected String globalName;
    private boolean enableNotifications;
    private String logException;

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
        //let mule do the default handling
        return event;
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
        for (MappingExceptionListener exceptionListener : exceptionListeners)
        {
            exceptionListener.setEnableNotifications(enableNotifications);
            if (logException != null)
            {
                exceptionListener.setLogException(logException);
            }
        }
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

    @Override
    public void addMessageProcessorPathElements(MessageProcessorPathElement pathElement)
    {
        int idx = 0;
        for (MessagingExceptionHandlerAcceptor listener : exceptionListeners)
        {
            if (listener instanceof MessageProcessorContainer)
            {
                MessageProcessorPathElement exceptionListener = pathElement.addChild(String.valueOf(idx));
                ((MessageProcessorContainer) listener).addMessageProcessorPathElements(exceptionListener);
            }
            idx++;
        }

    }

    public void setEnableNotifications(boolean enableNotifications)
    {
        this.enableNotifications = enableNotifications;
    }

    public void setLogException(String logException)
    {
        this.logException = logException;
    }

}
