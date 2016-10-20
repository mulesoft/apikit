/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import static org.mule.module.apikit.CharsetUtils.getEncoding;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.DataType;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.cache.JsonSchemaCache;
import org.mule.module.apikit.validation.io.JsonUtils;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.raml.v2.internal.utils.StreamUtils;
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
    public void validate(String configId, String schemaPath, MuleEvent muleEvent, IRaml api) throws BadRequestException
    {
        try
        {
            JsonNode data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof InputStream)
            {
                logger.debug("transforming payload to perform JSON Schema validation");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    IOUtils.copyLarge((InputStream) input, baos);
                }
                finally
                {
                    IOUtils.closeQuietly((InputStream) input);
                }

                String encoding = getEncoding(muleEvent.getMessage(), baos.toByteArray(), logger);
                DataType<ByteArrayInputStream> dataType = DataTypeFactory.create(ByteArrayInputStream.class, muleEvent.getMessage().getDataType().getMimeType());
                dataType.setEncoding(encoding);
                muleEvent.getMessage().setPayload(new ByteArrayInputStream(baos.toByteArray()), dataType);

                //convert to string to remove BOM
                String str = StreamUtils.toString(new ByteArrayInputStream(baos.toByteArray()));
                data = JsonUtils.parseJson(new StringReader(str));
            }
            else if (input instanceof String)
            {
                data = JsonUtils.parseJson(new StringReader((String) input));
            }
            else if (input instanceof byte[])
            {
                String encoding = getEncoding(muleEvent.getMessage(), (byte[]) input, logger);
                input = StreamUtils.trimBom((byte[]) input);
                data = JsonUtils.parseJson(new InputStreamReader(new ByteArrayInputStream((byte[]) input), encoding));

                //update message encoding
                DataType<byte[]> dataType = DataTypeFactory.create(byte[].class, muleEvent.getMessage().getDataType().getMimeType());
                dataType.setEncoding(encoding);
                muleEvent.getMessage().setPayload(input, dataType);
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            JsonSchema schema = JsonSchemaCache.getJsonSchemaCache(muleContext, configId, api).get(schemaPath);
            ProcessingReport report = schema.validate(data);
            if (!report.isSuccess())
            {
                String message = report.iterator().hasNext() ? report.iterator().next().toString() : "no message";
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
