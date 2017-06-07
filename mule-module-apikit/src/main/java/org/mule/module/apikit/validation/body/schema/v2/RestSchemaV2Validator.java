/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v2;

import java.util.List;

import org.mule.module.apikit.ApikitErrorTypes;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;
import org.mule.raml.interfaces.model.IMimeType;
import org.raml.v2.api.model.common.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestSchemaV2Validator implements IRestSchemaValidatorStrategy {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private IMimeType mimeType;

  public RestSchemaV2Validator(IMimeType mimeType) {
    this.mimeType = mimeType;
  }

  public void validate(String payload) throws BadRequestException {
    List<ValidationResult> validationResults;

    if (mimeType instanceof org.mule.raml.implv2.v10.model.MimeTypeImpl) {
      validationResults = ((org.mule.raml.implv2.v10.model.MimeTypeImpl) mimeType).validate(payload);
    } else {
      // TODO implement for 08 (v2)
      // List<ValidationResult> validationResults = ((org.mule.raml.implv2.v08.model.MimeTypeImpl) mimeType).validate(payload);
      throw new RuntimeException("not supported");
    }

    if (!validationResults.isEmpty()) {
      String logMessage = validationResults.get(0).getMessage();
      logger.info("Schema validation failed: " + logMessage);
      throw ApikitErrorTypes.throwErrorTypeNew(new BadRequestException(logMessage));
    }
  }
}
