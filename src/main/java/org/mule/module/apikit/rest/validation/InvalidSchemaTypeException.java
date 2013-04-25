package org.mule.module.apikit.rest.validation;

import org.mule.module.apikit.rest.RestException;

public class InvalidSchemaTypeException extends RestException
{

    public InvalidSchemaTypeException()
    {
        super();
    }

    public InvalidSchemaTypeException(Throwable throwable)
    {
        super(throwable);
    }
}
