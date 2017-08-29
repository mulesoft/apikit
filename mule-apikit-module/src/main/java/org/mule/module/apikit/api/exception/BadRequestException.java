/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.exception;

public class BadRequestException extends MuleRestException {

  public static final String STRING_REPRESENTATION = "APIKIT:BAD_REQUEST";

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(Throwable e) {
    super(e);
  }

  public BadRequestException() {
    super();
  }

  @Override
  public String getStringRepresentation() {
    return STRING_REPRESENTATION;
  }
}
