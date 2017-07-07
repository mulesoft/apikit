/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues.exceptions;

import org.mule.module.apikit.api.exception.MuleRestException;

public class ConflictException extends MuleRestException {

    private static final long serialVersionUID = 3387516993124229969L;

    public ConflictException(String message) {
        super(message);
    }

    @Override
    public String getStringRepresentation()
    {
        return "CUSTOM:CONFLICT";
    }
}
