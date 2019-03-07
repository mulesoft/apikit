/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service;

/**
 * Represents an error occurred when parsing Scaffolder input files
 */
public interface ComponentScaffoldingError {

  /**
   * Detailed cause of the error
   *
   * @return a details cause of the error.
   */
  String cause();

  /**
   * Return the error type
   *
   * @return error type
   */
  ScaffoldingErrorType errorType();
}
