/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import static com.github.fge.jsonschema.core.report.LogLevel.ERROR;
import static com.github.fge.jsonschema.core.report.LogLevel.WARNING;

import org.apache.commons.lang.StringUtils;
import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.module.apikit.validation.body.schema.v1.io.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestJsonSchemaValidator implements IRestSchemaValidatorStrategy
{
    private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private JsonSchema jsonSchema;

    public RestJsonSchemaValidator(JsonSchema jsonSchema)
    {
        this.jsonSchema = jsonSchema;
    }

    @Override
    public void validate(String payload) throws BadRequestException {

        if (jsonSchema != null) {
            JsonNode data;
            ProcessingReport report;

            try {
                boolean isEmpty = StringUtils.isEmpty(payload);
                data = JsonUtils.parseJson(new StringReader(isEmpty? "null" : payload));
                report = jsonSchema.validate(data);
            } catch (IOException|ProcessingException e)
            {
                throw ApikitErrorTypes.throwErrorType(new BadRequestException(e));
            }


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
                    throw ApikitErrorTypes.throwErrorType(new BadRequestException(logMessage));
                }
            }
        }
    }
}
