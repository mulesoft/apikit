package org.mule.module.apikit.exception;

public class BadRequestException extends MuleRestException
{

    public BadRequestException(String message)
    {
        super(message);
    }

    public BadRequestException(Throwable e)
    {
        super(e);
    }
}
