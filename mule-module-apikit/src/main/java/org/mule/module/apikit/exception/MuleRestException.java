package org.mule.module.apikit.exception;

import org.mule.api.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

public class MuleRestException extends MuleException
{

    public MuleRestException(String message)
    {
        super(MessageFactory.createStaticMessage(message));
    }

    public MuleRestException(Message message, Throwable cause)
    {
        super(message, cause);
    }

    public MuleRestException(Throwable cause)
    {
        super(cause);
    }

    public MuleRestException()
    {
    }
}
