/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.parser.service.logger;

import org.slf4j.LoggerFactory;

class DefaultLogger implements Logger {

  private final org.slf4j.Logger logger;

  DefaultLogger(Class clazz) {
    logger = LoggerFactory.getLogger(clazz);
  }

  @Override
  public void debug(String msg) {
    logger.debug(msg);
  }

  @Override
  public void debug(String msg, Throwable error) {
    logger.debug(msg, error);
  }

  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  @Override
  public void info(String msg, Throwable error) {
    logger.info(msg, error);
  }

  @Override
  public void warn(String msg) {
    logger.warn(msg);
  }

  @Override
  public void warn(String msg, Throwable error) {
    logger.warn(msg, error);
  }

  @Override
  public void error(String msg) {
    logger.error(msg);
  }

  @Override
  public void error(String msg, Throwable error) {
    logger.error(msg, error);
  }
}
