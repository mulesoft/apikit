/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

/**
 * Extension (SPI) for the APIKit Module
 */
public interface RouterService
{

    /**
     * Handles the request and returns a valid MuleEvent
     *
     * @param event
     * @return
     */
    MuleEvent processBlockingRequest(MuleEvent event, MessageProcessor router) throws MuleException;

    /**
     * Returns true if the path of the HTTP request matches a predefined condition
     *
     * @param event
     * @return
     */
    boolean isExecutable(MuleEvent event);

}
