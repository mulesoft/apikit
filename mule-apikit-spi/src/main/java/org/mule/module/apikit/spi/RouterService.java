/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import java.util.concurrent.CompletableFuture;

import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

/**
 * Extension (SPI) for the APIKit Module
 */
public interface RouterService {

  /**
   * Handles the request and returns a valid MuleEvent
   *
   * @param event
  * @param ramlPath 
   * @return
   */
  CompletableFuture<Event> process(CoreEvent event, EventProcessor router, String ramlPath) throws MuleException;

  /**
   * Returns true if the path of the HTTP request matches a predefined condition
   *
   * @param event
   * @return
   */
  boolean isExecutable(CoreEvent event);

}
