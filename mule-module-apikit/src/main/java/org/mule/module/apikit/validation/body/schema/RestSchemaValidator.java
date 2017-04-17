/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import org.mule.module.apikit.ApikitRegistry;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.RestSchemaV1Validator;
import org.mule.module.apikit.validation.body.schema.v2.RestSchemaV2Validator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.message.Message;

public class RestSchemaValidator
{
    private IMimeType mimeType;
    private Configuration config;
    private IAction action;

    public RestSchemaValidator(IMimeType mimeType, Configuration config, IAction action)
    {
        this.mimeType = mimeType;
        this.config = config;
        this.action = action;
    }

    public Message validate(Message message) throws BadRequestException
    {
        IRestSchemaValidator validator = determineValidator();
        return validator.validate(message);
    }

    private IRestSchemaValidator determineValidator()
    {
        if(config.getRamlHandler().isParserV2())
        {
            return new RestSchemaV2Validator(mimeType);
        }
        else
        {
            return new RestSchemaV1Validator(config.getJsonSchemaCache(), config.getXmlSchemaCache(), action);
        }
    }
}
