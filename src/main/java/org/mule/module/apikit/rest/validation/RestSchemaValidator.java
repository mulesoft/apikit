package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleEvent;

public interface RestSchemaValidator
{
    void validate(String schemaLocation, MuleEvent muleEvent) throws InvalidInputException;
}
