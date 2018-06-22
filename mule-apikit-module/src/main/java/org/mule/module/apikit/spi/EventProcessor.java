/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import java.util.concurrent.CompletableFuture;

import org.mule.module.apikit.api.RamlHandler;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

public interface EventProcessor {

  public CompletableFuture<Event> processEvent(CoreEvent event) throws MuleException;

  public RamlHandler getRamlHandler();
}
