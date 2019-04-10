/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import static org.mule.parser.service.ScaffoldingErrorType.GENERATION;

/**
 * Represents a simple parsing error with cause message
 */
public class SimpleScaffoldingError implements ComponentScaffoldingError {

  private final String cause;
  private final ScaffoldingErrorType errorType;

  public SimpleScaffoldingError(String cause, ScaffoldingErrorType errorType) {
    this.cause = cause;
    this.errorType = errorType;
  }

  public SimpleScaffoldingError(String cause) {
    this.cause = cause;
    this.errorType = GENERATION;
  }

  @Override
  public String cause() {
    return cause;
  }

  @Override
  public ScaffoldingErrorType errorType() {
    return errorType;
  }
}
