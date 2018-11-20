/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.logger;

public interface Logger {

  void debug(String msg);

  void debug(String msg, Throwable error);

  void info(String msg);

  void info(String msg, Throwable error);

  void warn(String msg);

  void warn(String msg, Throwable error);

  void error(String msg);

  void error(String msg, Throwable error);
}
