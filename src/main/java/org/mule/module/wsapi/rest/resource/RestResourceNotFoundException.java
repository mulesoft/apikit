package org.mule.module.wsapi.rest.resource;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.config.i18n.CoreMessages;

public class RestResourceNotFoundException extends MessagingException
{
    public RestResourceNotFoundException(String message, MuleEvent event, Throwable cause)
    {
        super(CoreMessages.createStaticMessage(message), event, cause);
    }

    public RestResourceNotFoundException(String message, MuleEvent event)
    {
        super(CoreMessages.createStaticMessage(message), event);
    }

    public RestResourceNotFoundException(Throwable throwable, MuleEvent event)
    {
        super(CoreMessages.createStaticMessage(throwable.getMessage()), event, throwable);
    }
}
