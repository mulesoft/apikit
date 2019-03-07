/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

import java.util.List;

/**
 * Represents a parsing error, with description and a list of children errors
 */
public class CompositeScaffoldingError implements ComponentScaffoldingError {

  private final String description;
  private final ScaffoldingErrorType errorType;
  private final List<SimpleScaffoldingError> errors;

  public CompositeScaffoldingError(String description, ScaffoldingErrorType errorType, List<SimpleScaffoldingError> errors) {
    this.description = description;
    this.errorType = errorType;
    this.errors = errors;
  }

  @Override
  public String cause() {
    StringBuilder builder = new StringBuilder(description);
    builder.append(":");
    errors.forEach(error -> {
      builder.append("\n");
      builder.append(error.cause());
    });
    return builder.toString();
  }

  @Override
  public ScaffoldingErrorType errorType() {
    return errorType;
  }
}
