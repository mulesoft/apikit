package org.mule.module.apikit.exception;

public class InvalidQueryParameterException extends BadRequestException
{

    public InvalidQueryParameterException(String message)
    {
        super(message);
    }
}
