package org.mule.module.apikit.exception;

public class InvalidFormParameterException extends BadRequestException
{

    public InvalidFormParameterException(String message)
    {
        super(message);
    }
}
