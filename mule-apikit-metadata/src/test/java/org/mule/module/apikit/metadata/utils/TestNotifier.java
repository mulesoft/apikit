/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.util.Arrays;
import org.mule.module.apikit.metadata.api.Notifier;
import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestNotifier implements Notifier {

  public static final String ERROR = "error";
  public static final String DEBUG = "debug";
  public static final String INFO = "info";
  public static final String WARN = "warn";
  public MultiMap<String, String> messages = new MultiMap<>();

  public static final Logger logger = LoggerFactory.getLogger(TestNotifier.class);

  @Override
  public void error(String message) {
    messages.put(ERROR, message);
    log(ERROR, message);
  }

  @Override
  public void warn(String message) {
    messages.put(WARN, message);
    log(WARN, message);
  }

  @Override
  public void info(String message) {
    messages.put(INFO, message);
    logger.info(message);
  }

  @Override
  public void debug(String message) {
    messages.put(DEBUG, message);
    logger.debug(message);
  }

  public List<String> messages(String type) {
    return messages.getAll(type);

  }

  private static void log(final String level, final String msg) {

    final String text = level.toUpperCase() + ": " + msg;
    final String separator = repeatChar('*', text.length());
    System.out.println(separator);
    System.out.println(text);
    System.out.println(separator);
  }

  private static final String repeatChar(char c, int length) {
    char[] data = new char[length];
    Arrays.fill(data, c);
    return new String(data);
  }
}
