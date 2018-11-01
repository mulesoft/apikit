/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.api;

/**
 * Interface to send information
 */
public interface Notifier {

  /**
   * Level ERROR
   * @param message
   */
  void error(String message);

  /**
   * Level WARNING
   * @param message
   */
  void warn(String message);

  /**
   * Level INFO
   * @param message
   */
  void info(String message);

  /**
   * Level DEBUG
   * @param message
   */
  void debug(String message);
}
