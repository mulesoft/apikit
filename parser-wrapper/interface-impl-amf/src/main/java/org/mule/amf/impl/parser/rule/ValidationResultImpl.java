/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.amf.impl.parser.rule;

import amf.client.validate.ValidationResult;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.rule.Severity;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.mule.raml.interfaces.parser.rule.Severity.ERROR;

public class ValidationResultImpl implements IValidationResult {

  private ValidationResult validationResult;
  private List<String> severities;

  public ValidationResultImpl(ValidationResult validationResult) {
    this.validationResult = validationResult;
    severities = newArrayList(Severity.values()).stream().map(Enum::name).collect(toList());
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

  @Override
  public Severity getSeverity() {
    if (!severities.contains(validationResult.level()))
      return ERROR;
    return Severity.fromString(validationResult.level());
  }
}
