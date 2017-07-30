/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.exception;

import org.mule.module.apikit.api.exception.MuleRestException;

public class MethodNotAllowedException extends MuleRestException {

  public static final String STRING_REPRESENTATION = "APIKIT:METHOD_NOT_ALLOWED";

  public MethodNotAllowedException(String message) {
    super(message);
  }

  public MethodNotAllowedException(Throwable t) {
    super(t);
  }

  public MethodNotAllowedException(){
    super("Method not allowed");
  }

  @Override
  public String getStringRepresentation()
  {
    return STRING_REPRESENTATION;
  }
}
