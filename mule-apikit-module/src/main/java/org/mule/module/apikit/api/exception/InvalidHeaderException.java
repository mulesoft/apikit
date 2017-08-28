/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.exception;

public class InvalidHeaderException extends BadRequestException {

  public static final String STRING_REPRESENTATION = "APIKIT:BAD_REQUEST";

  public InvalidHeaderException(String message) {
    super(message);
  }

  public InvalidHeaderException(Throwable t) {
    super(t);
  }

  public InvalidHeaderException() {
    super("Invalid Header");
  }

  @Override
  public String getStringRepresentation() {
    return STRING_REPRESENTATION;
  }
}
