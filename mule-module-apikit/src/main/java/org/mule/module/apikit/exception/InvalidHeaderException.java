package org.mule.module.apikit.exception;

public class InvalidHeaderException extends BadRequestException
{

    public InvalidHeaderException(String message)
    {
        super(message);
    }
}
