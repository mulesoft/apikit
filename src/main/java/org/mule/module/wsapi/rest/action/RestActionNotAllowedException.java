package org.mule.module.wsapi.rest.action;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.config.i18n.CoreMessages;

public class RestActionNotAllowedException extends MessagingException
{
    public RestActionNotAllowedException(String message, MuleEvent event, Throwable cause)
    {
        super(CoreMessages.createStaticMessage(message), event, cause);
    }

    public RestActionNotAllowedException(String message, MuleEvent event)
    {
        super(CoreMessages.createStaticMessage(message), event);
    }

    public RestActionNotAllowedException(Throwable throwable, MuleEvent event)
    {
        super(CoreMessages.createStaticMessage(throwable.getMessage()), event, throwable);
    }
}
