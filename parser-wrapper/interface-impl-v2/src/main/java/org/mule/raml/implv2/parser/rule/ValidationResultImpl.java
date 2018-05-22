/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.parser.rule;

import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.v2.api.model.common.ValidationResult;

public class ValidationResultImpl implements IValidationResult {

  private final ValidationResult validationResult;

  public ValidationResultImpl(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  @Override
  public String getMessage() {
    return validationResult.getMessage();
  }

  @Override
  public String getPath() {
    return validationResult.getPath();
  }

  @Override
  public String getIncludeName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLine() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLineUnknown() {
    throw new UnsupportedOperationException();
  }
}
