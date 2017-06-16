/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.exception;

public class NotFoundException extends MuleRestException
{
  public static final String STRING_REPRESENTATION = "APIKIT:NOT_FOUND";

  public NotFoundException(String path) {
    super(path);
  }

  public NotFoundException(Throwable t) {
    super(t);
  }

  public NotFoundException(){
    super();
  }

  @Override
  public String getStringRepresentation()
  {
    return STRING_REPRESENTATION;
  }
}
