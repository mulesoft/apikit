/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.parser.rule;

import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.parser.rule.ValidationResult;

public class ValidationResultImpl implements IValidationResult
{
    ValidationResult validationResult;
    public ValidationResultImpl(ValidationResult validationResult)
    {
        this.validationResult = validationResult;
    }

    public String getMessage()
    {
        return validationResult.getMessage();
    }

    public String getIncludeName()
    {
        return validationResult.getIncludeName();
    }

    public int getLine()
    {
        return validationResult.getLine();
    }

    public boolean isLineUnknown()
    {
        return validationResult.getLine() == ValidationResult.UNKNOWN;
    }
}
