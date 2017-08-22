/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import org.mule.module.apikit.api.exception.BadRequestException;

public class RestSchemaValidator {

  IRestSchemaValidatorStrategy strategy;

  public RestSchemaValidator(IRestSchemaValidatorStrategy schemaValidationStrategy) {
    this.strategy = schemaValidationStrategy;
  }

  public void validate(String payload) throws BadRequestException {
    this.strategy.validate(payload);
  }

}
