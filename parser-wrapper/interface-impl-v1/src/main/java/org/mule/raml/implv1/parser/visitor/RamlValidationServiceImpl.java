/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.parser.visitor;

import java.util.ArrayList;
import java.util.List;

import org.mule.raml.implv1.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlValidationService;

public class RamlValidationServiceImpl implements IRamlValidationService
{
    RamlDocumentBuilderImpl ramlDocumentBuilderImpl;
    List<IValidationResult> errors;
    List<IValidationResult> warnings;

    public RamlValidationServiceImpl(IRamlDocumentBuilder ramlDocumentBuilder)
    {
        ramlDocumentBuilderImpl = (RamlDocumentBuilderImpl) ramlDocumentBuilder.getInstance();
    }

    public IRamlValidationService validate(String resource)
    {
        return validate(null, resource);
    }

    public IRamlValidationService validate(String resourceContent, String resource)
    {
        ResourceLoader resourceLoader = ramlDocumentBuilderImpl.getResourceLoader();
        List<ValidationResult> results = new ArrayList<ValidationResult>();
        results = RamlValidationService.createDefault(resourceLoader).validate(resourceContent, resource);
        errors = new ArrayList<IValidationResult>();
        for (ValidationResult validationResult : ValidationResult.getLevel(ValidationResult.Level.ERROR, results))
        {
            errors.add(new ValidationResultImpl(validationResult));
        }

        warnings = new ArrayList<IValidationResult>();
        for (ValidationResult validationResult : ValidationResult.getLevel(ValidationResult.Level.WARN, results))
        {
            warnings.add(new ValidationResultImpl(validationResult));
        }
        return this;
    }

    public List<IValidationResult> getErrors()
    {
        return errors;
    }

    public List<IValidationResult> getWarnings()
    {
        return warnings;
    }
}
