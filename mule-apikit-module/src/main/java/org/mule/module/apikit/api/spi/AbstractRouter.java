/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api.spi;

import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;

import org.reactivestreams.Publisher;

public interface AbstractRouter {

  Publisher<CoreEvent> processEvent(CoreEvent event) throws MuleException;

  IRaml getRaml();
}
