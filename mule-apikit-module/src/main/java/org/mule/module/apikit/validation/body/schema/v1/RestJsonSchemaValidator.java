/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import org.apache.commons.lang.StringUtils;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.module.apikit.validation.body.schema.v1.io.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import static com.github.fge.jsonschema.core.report.LogLevel.ERROR;
import static com.github.fge.jsonschema.core.report.LogLevel.WARNING;

public class RestJsonSchemaValidator implements IRestSchemaValidatorStrategy {

  private static final String JSON_SCHEMA_FAIL_ON_WARNING_KEY = "raml.json_schema.fail_on_warning";

  protected static final Logger logger = LoggerFactory.getLogger(RestJsonSchemaValidator.class);

  private JsonSchema jsonSchema;

  public RestJsonSchemaValidator(JsonSchema jsonSchema) {
    this.jsonSchema = jsonSchema;
  }

  @Override
  public void validate(String payload) throws BadRequestException {

    if (jsonSchema != null) {
      JsonNode data;
      ProcessingReport report;

      try {
        boolean isEmpty = StringUtils.isEmpty(payload);
        data = JsonUtils.parseJson(new StringReader(isEmpty ? "null" : payload));
        report = jsonSchema.validate(data, true);
      } catch (IOException | ProcessingException e) {
        throw new BadRequestException(e);
      }


      Iterator<ProcessingMessage> iterator = report.iterator();
      final StringBuilder messageBuilder = new StringBuilder();

      while (iterator.hasNext()) {
        ProcessingMessage next = iterator.next();
        LogLevel logLevel = next.getLogLevel();
        String logMessage = next.toString();

        boolean failOnWarning = Boolean.valueOf(
                                                System.getProperty(JSON_SCHEMA_FAIL_ON_WARNING_KEY, "false"));

        if (logLevel.equals(ERROR) || (logLevel.equals(WARNING) && failOnWarning)) {
          messageBuilder.append(logMessage).append("\n");
        }
      }

      if (messageBuilder.length() > 0) {
        String message = messageBuilder.toString();
        logger.info("Schema validation failed: " + message);
        throw new BadRequestException(message);
      }
    }
  }
}
