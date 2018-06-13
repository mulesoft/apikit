/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.rule;

import amf.client.validate.ValidationResult;
import org.mule.raml.interfaces.parser.rule.IValidationResult;

public class ValidationResultImpl implements IValidationResult {

  ValidationResult validationResult;

  public ValidationResultImpl(ValidationResult validationResult) {
    this.validationResult = validationResult;
  }

  public String getMessage() {
    return validationResult.toString();
  }

  public String getIncludeName() {
    return null;
  }

  public int getLine() {
    return -1;
  }

  public boolean isLineUnknown() {
    return false;
  }

  public String getPath() {
    throw new UnsupportedOperationException();
  }
}
