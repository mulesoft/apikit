package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleContext;
import org.mule.module.apikit.rest.representation.SchemaType;

public final class RestSchemaValidatorFactory
{

    private static RestSchemaValidatorFactory INSTANCE;

    static
    {
        INSTANCE = new RestSchemaValidatorFactory();
    }

    private RestSchemaValidatorFactory()
    {
    }

    public static RestSchemaValidatorFactory getInstance()
    {
        return INSTANCE;
    }

    public RestSchemaValidator createValidator(SchemaType schemaType, MuleContext muleContext) throws InvalidSchemaTypeException
    {
        if (schemaType == SchemaType.JSONSchema)
        {
            return new RestJsonSchemaValidator(muleContext);
        }
        else if (schemaType == SchemaType.XMLSchema)
        {
            return new RestXmlSchemaValidator(muleContext);
        }

        throw new InvalidSchemaTypeException();
    }
}
