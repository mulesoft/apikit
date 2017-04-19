/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.AttributesHelper;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.MessageHelper;
import org.mule.module.apikit.exception.MuleRestException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.validation.body.FormParametersValidator;
import org.mule.module.apikit.validation.body.schema.RestSchemaValidator;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.runtime.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodyValidator
{
    Configuration config;
    IAction action;

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    public BodyValidator(Configuration config, IAction action)
    {
        this.config = config;
        this.action = action;
    }

    public Message validate(Message message) throws MuleRestException
    {
        if (action == null || !action.hasBody())
        {
            logger.debug("=== no body types defined: accepting any request content-type");
            return message;
        }

        Message newMessage = message;
        String requestMimeTypeName = AttributesHelper.getMediaType((HttpRequestAttributes) message.getAttributes().getValue());
        boolean found = false;
        for (String mimeTypeName : action.getBody().keySet())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing request media type %s with expected %s\n",
                                           requestMimeTypeName, mimeTypeName));
            }
            if (mimeTypeName.equals(requestMimeTypeName))
            {
                found = true;
                IMimeType mimeType = action.getBody().get(mimeTypeName);

                RestSchemaValidator schemaValidator = new RestSchemaValidator(mimeType, config, action);
                newMessage = schemaValidator.validate(message);

                FormParametersValidator formParametersValidator = new FormParametersValidator(mimeType, config.getRamlHandler().isParserV2());
                newMessage = formParametersValidator.validate(newMessage);

                break;
            }
        }

        if (!found)
        {
            throw new UnsupportedMediaTypeException();
        }
        return newMessage;
    }
}
