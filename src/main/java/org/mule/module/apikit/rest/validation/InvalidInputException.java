package org.mule.module.apikit.rest.validation;

import org.mule.module.apikit.rest.RestException;

public class InvalidInputException extends RestException
{

    public InvalidInputException()
    {
        super();
    }

    public InvalidInputException(String message)
    {
        super(message);
    }

    public InvalidInputException(Throwable throwable)
    {
        super(throwable);
    }
}
