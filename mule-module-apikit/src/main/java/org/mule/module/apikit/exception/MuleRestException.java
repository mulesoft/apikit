/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.exception;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.i18n.I18nMessageFactory;

public abstract class MuleRestException extends MuleException
{
  public MuleRestException(String message) {
    super(I18nMessageFactory.createStaticMessage(message));
  }

  public MuleRestException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public MuleRestException(Throwable cause) {
    super(cause);
  }

  public MuleRestException() {}

  public abstract String getStringRepresentation();
}
