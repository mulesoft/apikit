/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.parser.rule;

public interface IValidationResult {

  String getMessage();

  String getIncludeName();

  int getLine();

  boolean isLineUnknown();

  String getPath();

  Severity getSeverity();

  static IValidationResult fromException(final Exception e) {
    return new IValidationResult() {

      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public String getIncludeName() {
        return null;
      }

      @Override
      public int getLine() {
        return 0;
      }

      @Override
      public boolean isLineUnknown() {
        return false;
      }

      @Override
      public String getPath() {
        return null;
      }

      @Override
      public Severity getSeverity() {
        return null;
      }
    };
  }
}
