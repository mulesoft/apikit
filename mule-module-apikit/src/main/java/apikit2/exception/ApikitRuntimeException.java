package apikit2.exception;

import org.mule.api.MuleRuntimeException;
import org.mule.config.i18n.MessageFactory;

public class ApikitRuntimeException extends MuleRuntimeException
{

    public ApikitRuntimeException(String message)
    {
        super(MessageFactory.createStaticMessage(message));
    }

    public ApikitRuntimeException(Throwable t)
    {
        super(t);
    }
}
