/*
 * Copyright 2013 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
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
