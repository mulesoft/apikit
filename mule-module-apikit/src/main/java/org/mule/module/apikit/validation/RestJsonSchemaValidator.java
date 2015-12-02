/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.DataType;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.cache.JsonSchemaCache;
import org.mule.transformer.types.DataTypeFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.raml.model.Raml;
import org.raml.parser.utils.StreamUtils;
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
    public void validate(String configId, String schemaPath, MuleEvent muleEvent, Raml api) throws BadRequestException
    {
        try
        {
            JsonNode data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof InputStream)
            {
                input = StreamUtils.toString((InputStream) input);
                logger.debug("transforming payload to perform JSON Schema validation");
                DataType<String> dataType = DataTypeFactory.create(String.class, muleEvent.getMessage().getDataType().getMimeType());
                muleEvent.getMessage().setPayload(input, dataType);
            }
            if (input instanceof String)
            {
                data = JsonLoader.fromReader(new StringReader((String) input));
            }
            else if (input instanceof byte[])
            {
                data = JsonLoader.fromReader(new InputStreamReader(new ByteArrayInputStream((byte[]) input)));
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            JsonSchema schema = JsonSchemaCache.getJsonSchemaCache(muleContext, configId, api).get(schemaPath);
            ProcessingReport report = schema.validate(data);
            if (!report.isSuccess())
            {
                String message = report.iterator().hasNext() ? report.iterator().next().getMessage() : "no message";
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
        catch (ProcessingException e)
        {
            throw new BadRequestException(e);
        }
    }
}
