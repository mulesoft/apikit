/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.runtime.api.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestNotifier implements Notifier {

  private static final String ERROR = "error";
  private static final String DEBUG = "debug";
  private static final String INFO = "info";
  private static final String WARN = "warn";
  private Logger log = LoggerFactory.getLogger(TestNotifier.class);
  private MultiMap<String, String> messages = new MultiMap<>();

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

  public List<String> error() {
    return messages.getAll(ERROR);
  }

  public List<String> warn() {
    return messages.getAll(WARN);
  }

  public List<String> info() {
    return messages.getAll(INFO);
  }

  public List<String> debug() {
    return messages.getAll(DEBUG);
  }
}
