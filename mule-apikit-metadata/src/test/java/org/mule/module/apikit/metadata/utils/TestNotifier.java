/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

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
  public Logger log = LoggerFactory.getLogger(TestNotifier.class);
  public MultiMap<String, String> messages = new MultiMap<>();

  @Override
  public void error(String message) {
    messages.put(ERROR, message);
    log.error(message);
  }

  @Override
  public void warn(String message) {
    messages.put(WARN, message);
    log.warn(message);
  }

  @Override
  public void info(String message) {
    messages.put(INFO, message);
    log.info(message);
  }

  @Override
  public void debug(String message) {
    messages.put(DEBUG, message);
    log.debug(message);
  }

  public List<String> messages(String type) {
    return messages.getAll(type);

  }
}
