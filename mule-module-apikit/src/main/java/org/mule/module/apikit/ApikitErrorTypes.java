/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.exception.MuleRestException;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.exception.ErrorTypeRepositoryFactory;
import org.mule.runtime.core.exception.TypedException;

public class ApikitErrorTypes
{
    private static MuleContext muleContext;

    public static void initialise(MuleContext muleContext)
    {
        ApikitErrorTypes.muleContext = muleContext;
    }

    public static TypedException throwErrorTypeNew(MuleRestException exception)
    {
        ComponentIdentifier componentIdentifier = ComponentIdentifier.buildFromStringRepresentation(exception.getStringRepresentation());
        if (muleContext != null)
        {
            ErrorType errorType = muleContext.getErrorTypeRepository().getErrorType(componentIdentifier).get();
            if (errorType != null)
            {
                return new TypedException(exception, errorType);
            }
        }
        return new TypedException(exception, ErrorTypeRepositoryFactory.createDefaultErrorTypeRepository().getAnyErrorType());
    }
}
