/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import static com.github.fge.jsonschema.core.report.LogLevel.ERROR;
import static com.github.fge.jsonschema.core.report.LogLevel.WARNING;
import static org.mule.module.apikit.CharsetUtils.getEncoding;

import org.mule.module.apikit.CharsetUtils;
import org.mule.module.apikit.MessageHelper;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.v1.io.JsonUtils;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.DataTypeBuilder;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.util.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.google.common.cache.LoadingCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.raml.parser.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestJsonSchemaValidator
{
    private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private LoadingCache<String, JsonSchema> schemaCache;

    public RestJsonSchemaValidator(LoadingCache<String, JsonSchema> schemaCache)
    {
        this.schemaCache = schemaCache;
    }

    public Message validate(String schemaPath, Message message) throws BadRequestException
    {
        Message newMessage = message;
        try
        {
            JsonNode data;
            Object input = message.getPayload().getValue();
            if (input instanceof CursorStreamProvider)
            {
                //TODO supoport cursorStreams
            }
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

                String charset = CharsetUtils.getEncoding(message, baos.toByteArray(), logger);
                DataType dataType = message.getPayload().getDataType();

                DataTypeBuilder sourceDataTypeBuilder = DataType.builder();
                sourceDataTypeBuilder.type(message.getPayload().getClass());
                sourceDataTypeBuilder.mediaType(dataType.getMediaType());
                sourceDataTypeBuilder.charset(charset);
                DataType sourceDataType = sourceDataTypeBuilder.build();//DataTypeFactory.create(event.getMessage().getPayload().getClass(), msgMimeType);
                newMessage = MessageHelper.setPayload(message, new ByteArrayInputStream(baos.toByteArray()), sourceDataType.getMediaType());

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
                String encoding = getEncoding(message, (byte[]) input, logger);
                input = org.raml.v2.internal.utils.StreamUtils.trimBom((byte[]) input);
                data = JsonUtils.parseJson(new InputStreamReader(new ByteArrayInputStream((byte[]) input), encoding));

                //update message encoding

                newMessage = MessageHelper.setPayload(message, input, message.getPayload().getDataType().getMediaType());
            }
            else
            {
                throw new BadRequestException("Don't know how to parse " + input.getClass().getName());
            }

            JsonSchema schema = schemaCache.get(schemaPath);
            ProcessingReport report = schema.validate(data);
            Iterator<ProcessingMessage> iterator = report.iterator();

            while (iterator.hasNext())
            {
                ProcessingMessage next = iterator.next();
                LogLevel logLevel = next.getLogLevel();
                String logMessage = next.toString();

                boolean failOnWarning = Boolean.valueOf(
                        System.getProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY, "false"));

                if (logLevel.equals(ERROR) || (logLevel.equals(WARNING) && failOnWarning))
                {
                    logger.info("Schema validation failed: " + logMessage);
                    throw new BadRequestException(logMessage);
                }
            }
        }
        catch (ExecutionException|IOException|ProcessingException e)
        {
            throw new BadRequestException(e);
        }
        return newMessage;
    }
}
