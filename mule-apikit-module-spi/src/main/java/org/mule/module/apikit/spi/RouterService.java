/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;

/**
 * Extension (SPI) for the APIKit Module
 */
public interface RouterService {

  /**
   * Handles the request and returns a valid MuleEvent
   *
   * @param event		the requester event
   * @param router 		reference to the apikit router
   * @param ramlPath 	path to the raml
   * @return 			a competable future with the response event
   */
  Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router, String ramlPath) throws MuleException;

}
