/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.registry.RegistrationException;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaAndNode;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaCache;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import apikit2.exception.BadRequestException;
import heaven.model.Heaven;
import org.eel.kitchen.jsonschema.report.ValidationReport;
import org.eel.kitchen.jsonschema.util.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestJsonSchemaValidator extends AbstractRestSchemaValidator
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RestJsonSchemaValidator(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void validate(String schemaLocation, MuleEvent muleEvent) throws InvalidInputException
    {
        try
        {
            validate(schemaLocation, muleEvent, null);
        }
        catch (BadRequestException badRequestException)
        {
            throw new InvalidInputException(badRequestException);
        }
    }

    public void validate(String schemaPath, MuleEvent muleEvent, Heaven api) throws BadRequestException
    {
        try
        {
            JsonNode data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof String)
            {
                data = JsonLoader.fromReader(new StringReader((String) input));
            }
            else if (input instanceof InputStream)
            {
                data = JsonLoader.fromReader(new InputStreamReader((InputStream) input));
            }
            else if (input instanceof byte[])
            {
                data = JsonLoader.fromReader(new InputStreamReader(new ByteArrayInputStream((byte[]) input)));
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            JsonSchemaAndNode schema = JsonSchemaCache.getJsonSchemaCache(muleContext, api).get(schemaPath);

            ValidationReport report = schema.getJsonSchema().validate(data);

            if (!report.isSuccess())
            {
                String message = report.getMessages().get(0);
                logger.info("Schema validation failed: " + message);
                throw new BadRequestException(message);
            }
        }
        catch (ExecutionException e)
        {
            throw new BadRequestException(e);
        }
        catch (RegistrationException e)
        {
            throw new BadRequestException(e);
        }
        catch (IOException e)
        {
            throw new BadRequestException(e);
        }
    }
}
