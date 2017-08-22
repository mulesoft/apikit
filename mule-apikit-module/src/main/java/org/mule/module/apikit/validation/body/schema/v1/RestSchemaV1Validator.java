/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidatorStrategy;

public class RestSchemaV1Validator implements IRestSchemaValidatorStrategy {

  IRestSchemaValidatorStrategy strategy;

  public RestSchemaV1Validator(IRestSchemaValidatorStrategy strategy) {
    this.strategy = strategy;
  }

  public void validate(String payload) throws BadRequestException {
    this.strategy.validate(payload);
  }
}
