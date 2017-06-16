/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces.parser.visitor;

import org.mule.raml.interfaces.parser.rule.IValidationResult;

import java.util.List;

public interface IRamlValidationService
{
    IRamlValidationService validate(String resource);

    IRamlValidationService validate(String resourceContent, String resource);

    List<IValidationResult> getErrors();

    List<IValidationResult> getWarnings();
}
