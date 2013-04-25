package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleContext;

public abstract class AbstractRestSchemaValidator implements RestSchemaValidator
{

    protected final MuleContext muleContext;

    public AbstractRestSchemaValidator(MuleContext muleContext)
    {
        this.muleContext = muleContext;
    }
}
