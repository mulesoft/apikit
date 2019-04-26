/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.spi;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

import org.reactivestreams.Publisher;

/**
 * Extension (SPI) for the APIKit Module
 */
public interface RouterService {

  /**
   * Initialise the extension
   *
   * @param ramlPath 	path to the raml.
   * @return 			    a initialized with the given raml RouterService
   */
  RouterService initialise(String ramlPath) throws MuleException;

  /**
   * Handles the request and returns a valid MuleEvent
   *
   * @param event		the requester event
   * @param router 		reference to the apikit router
   * @return 			a competable future with the response event
   */
  Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router) throws MuleException;
}
