package org.mule.module.apikit.exception;

public class NotFoundException extends MuleRestException
{

    public NotFoundException(String path)
    {
        super(path);
    }
}
