/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.exception;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessageFactory;

public class ApikitRuntimeException extends MuleRuntimeException {

  public ApikitRuntimeException(String message) {
    super(I18nMessageFactory.createStaticMessage(message));
  }

  public ApikitRuntimeException(String message, Throwable t) {
    super(I18nMessageFactory.createStaticMessage(message), t);
  }

  public ApikitRuntimeException(Throwable t) {
    super(t);
  }
}
